// Custom hook for memory-safe async operations
import { useState, useEffect, useCallback, useRef } from 'react';
import { logger } from '../utils/logger';

// Generic async operation state
export interface AsyncOperationState<T> {
  data: T | null;
  loading: boolean;
  error: Error | null;
}

// Options for async operations
export interface AsyncOperationOptions {
  immediate?: boolean; // Execute immediately on mount
  retryCount?: number; // Number of retries on failure
  retryDelay?: number; // Delay between retries in ms
}

// Custom hook for safe async operations with memory leak prevention
export const useAsyncOperation = <T>(
  operation: () => Promise<T>,
  dependencies: any[] = [],
  options: AsyncOperationOptions = {}
): AsyncOperationState<T> & {
  execute: () => Promise<void>;
  reset: () => void;
} => {
  const [state, setState] = useState<AsyncOperationState<T>>({
    data: null,
    loading: false,
    error: null
  });

  // Track if component is still mounted
  const isMountedRef = useRef(true);
  const abortControllerRef = useRef<AbortController | null>(null);
  const retryTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const {
    immediate = false,
    retryCount = 0,
    retryDelay = 1000
  } = options;

  // Cleanup function
  const cleanup = useCallback(() => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      abortControllerRef.current = null;
    }
    if (retryTimeoutRef.current) {
      clearTimeout(retryTimeoutRef.current);
      retryTimeoutRef.current = null;
    }
  }, []);

  // Execute the async operation with retry logic
  const executeOperation = useCallback(async (attempt: number = 0): Promise<void> => {
    if (!isMountedRef.current) return;

    try {
      // Create new abort controller for this operation
      cleanup();
      abortControllerRef.current = new AbortController();

      if (!isMountedRef.current) return;

      setState(prev => ({ ...prev, loading: true, error: null }));

      logger.debug('Async operation started', 'ASYNC_OPERATION', {
        attempt: attempt + 1,
        maxRetries: retryCount + 1
      });

      const result = await operation();

      // Check if component is still mounted before updating state
      if (isMountedRef.current && !abortControllerRef.current?.signal.aborted) {
        setState({
          data: result,
          loading: false,
          error: null
        });

        logger.debug('Async operation completed successfully', 'ASYNC_OPERATION', {
          attempt: attempt + 1
        });
      }
    } catch (error) {
      // Only handle error if component is still mounted and operation wasn't aborted
      if (isMountedRef.current && !abortControllerRef.current?.signal.aborted) {
        const errorObj = error instanceof Error ? error : new Error('Unknown error');

        // Retry logic
        if (attempt < retryCount) {
          logger.warn('Async operation failed, retrying', 'ASYNC_OPERATION', {
            attempt: attempt + 1,
            maxRetries: retryCount + 1,
            error: errorObj.message,
            retryDelay
          });

          retryTimeoutRef.current = setTimeout(() => {
            if (isMountedRef.current) {
              executeOperation(attempt + 1);
            }
          }, retryDelay);
        } else {
          logger.error('Async operation failed after all retries', 'ASYNC_OPERATION', {
            totalAttempts: attempt + 1,
            error: errorObj.message
          });

          setState({
            data: null,
            loading: false,
            error: errorObj
          });
        }
      }
    }
  }, [operation, retryCount, retryDelay, cleanup]);

  // Public execute function
  const execute = useCallback(async (): Promise<void> => {
    await executeOperation();
  }, [executeOperation]);

  // Reset function
  const reset = useCallback(() => {
    cleanup();
    if (isMountedRef.current) {
      setState({
        data: null,
        loading: false,
        error: null
      });
    }
  }, [cleanup]);

  // Effect to handle dependencies and immediate execution
  useEffect(() => {
    if (immediate) {
      executeOperation();
    }
    
    // Cleanup function for effect
    return () => {
      cleanup();
    };
  }, dependencies); // eslint-disable-line react-hooks/exhaustive-deps

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      isMountedRef.current = false;
      cleanup();
    };
  }, [cleanup]);

  return {
    ...state,
    execute,
    reset
  };
};

// Hook for multiple async operations with dependency management
export const useAsyncOperations = <T extends Record<string, any>>(
  operations: { [K in keyof T]: () => Promise<T[K]> },
  dependencies: any[] = []
): {
  data: Partial<T>;
  loading: boolean;
  errors: Partial<Record<keyof T, Error>>;
  execute: (keys?: (keyof T)[]) => Promise<void>;
  reset: (keys?: (keyof T)[]) => void;
} => {
  const [data, setData] = useState<Partial<T>>({});
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Partial<Record<keyof T, Error>>>({});

  const isMountedRef = useRef(true);
  const abortControllersRef = useRef<Map<keyof T, AbortController>>(new Map());

  // Cleanup function
  const cleanup = useCallback((keys?: (keyof T)[]) => {
    const keysToClean = keys || Object.keys(operations) as (keyof T)[];
    
    keysToClean.forEach(key => {
      const controller = abortControllersRef.current.get(key);
      if (controller) {
        controller.abort();
        abortControllersRef.current.delete(key);
      }
    });
  }, [operations]);

  // Execute operations
  const execute = useCallback(async (keys?: (keyof T)[]): Promise<void> => {
    if (!isMountedRef.current) return;

    const keysToExecute = keys || Object.keys(operations) as (keyof T)[];
    
    setLoading(true);
    setErrors(prev => {
      const newErrors = { ...prev };
      keysToExecute.forEach(key => delete newErrors[key]);
      return newErrors;
    });

    const promises = keysToExecute.map(async (key) => {
      try {
        // Create abort controller for this operation
        const controller = new AbortController();
        abortControllersRef.current.set(key, controller);

        const result = await operations[key]();

        if (isMountedRef.current && !controller.signal.aborted) {
          setData(prev => ({ ...prev, [key]: result }));
        }
      } catch (error) {
        if (isMountedRef.current) {
          const errorObj = error instanceof Error ? error : new Error('Unknown error');
          setErrors(prev => ({ ...prev, [key]: errorObj }));
          
          logger.error(`Async operation failed for key: ${String(key)}`, 'ASYNC_OPERATIONS', {
            key: String(key),
            error: errorObj.message
          });
        }
      }
    });

    await Promise.allSettled(promises);

    if (isMountedRef.current) {
      setLoading(false);
    }
  }, [operations]);

  // Reset function
  const reset = useCallback((keys?: (keyof T)[]) => {
    cleanup(keys);
    
    if (isMountedRef.current) {
      const keysToReset = keys || Object.keys(operations) as (keyof T)[];
      
      setData(prev => {
        const newData = { ...prev };
        keysToReset.forEach(key => delete newData[key]);
        return newData;
      });
      
      setErrors(prev => {
        const newErrors = { ...prev };
        keysToReset.forEach(key => delete newErrors[key]);
        return newErrors;
      });
    }
  }, [cleanup, operations]);

  // Effect for dependencies
  useEffect(() => {
    execute();
    
    return () => {
      cleanup();
    };
  }, dependencies); // eslint-disable-line react-hooks/exhaustive-deps

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      isMountedRef.current = false;
      cleanup();
    };
  }, [cleanup]);

  return {
    data,
    loading,
    errors,
    execute,
    reset
  };
};

export default useAsyncOperation;
