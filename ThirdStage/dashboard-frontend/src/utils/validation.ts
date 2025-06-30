// Runtime type validation utilities for API responses

import { logger } from './logger';

export class ValidationError extends Error {
  constructor(message: string, public field?: string, public received?: any) {
    super(message);
    this.name = 'ValidationError';
  }
}

// Basic type validators
export const validators = {
  isString: (value: any): value is string => typeof value === 'string',
  isNumber: (value: any): value is number => typeof value === 'number' && !isNaN(value),
  isBoolean: (value: any): value is boolean => typeof value === 'boolean',
  isArray: (value: any): value is any[] => Array.isArray(value),
  isObject: (value: any): value is object => value !== null && typeof value === 'object' && !Array.isArray(value),
  isDate: (value: any): value is string => {
    if (typeof value !== 'string') return false;
    const date = new Date(value);
    return !isNaN(date.getTime());
  },
  isOptional: <T>(validator: (value: any) => value is T) => (value: any): value is T | undefined => {
    return value === undefined || value === null || validator(value);
  }
};

// Validation schema builder
export class ValidationSchema<T> {
  constructor(private validator: (value: any) => value is T) {}

  validate(data: any, context = 'data'): T {
    if (!this.validator(data)) {
      throw new ValidationError(`Invalid ${context}: expected valid structure`, context, data);
    }
    return data;
  }

  validateArray(data: any, context = 'array'): T[] {
    if (!validators.isArray(data)) {
      throw new ValidationError(`Invalid ${context}: expected array`, context, data);
    }
    
    return data.map((item, index) => {
      try {
        return this.validate(item, `${context}[${index}]`);
      } catch (error) {
        if (error instanceof ValidationError) {
          throw new ValidationError(
            `Invalid item at ${context}[${index}]: ${error.message}`,
            `${context}[${index}]`,
            item
          );
        }
        throw error;
      }
    });
  }
}

// API Response validators
export const apiValidators = {
  // Recent Job Build validation
  recentJobBuild: (data: any): data is import('../services/api').RecentJobBuildDTO => {
    if (!validators.isObject(data)) return false;

    const required = [
      ['jobName', validators.isString],
      ['buildId', validators.isNumber],
      ['originalJobName', validators.isString],
      ['healthStatus', validators.isString],
      ['anomalyCount', validators.isNumber],
      ['timeAgo', validators.isString],
      ['rawTimestamp', validators.isString],
      ['computedAt', validators.isString]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (data as any)[field];
      const isValid = validator(value);
      if (!isValid) {
        logger.warn(`Validation failed for field '${field}'`, 'VALIDATION', {
          field,
          value,
          expectedType: validator.name
        }, { component: 'RecentJobBuildValidator', action: 'validateField' });
      }
      return isValid;
    });
  },

  // Security Anomaly Count validation
  securityAnomalyCount: (data: any): data is import('../services/api').SecurityAnomalyCountDTO => {
    if (!validators.isObject(data)) return false;

    const required = [
      ['jobFilter', validators.isString],
      ['anomalyCount', validators.isNumber],
      ['timeRange', validators.isString],
      ['computedAt', validators.isString]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (data as any)[field];
      return validator(value);
    });
  },

  // Active Build Count validation
  activeBuildCount: (data: any): data is import('../services/api').ActiveBuildCountDTO => {
    if (!validators.isObject(data)) return false;

    const required = [
      ['jobFilter', validators.isString],
      ['activeBuilds', validators.isNumber],
      ['computedAt', validators.isString]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (data as any)[field];
      return validator(value);
    });
  },

  // Job Count validation
  jobCount: (data: any): data is import('../services/api').JobCountDTO => {
    if (!validators.isObject(data)) return false;

    const required = [
      ['timeBoundary', validators.isString],
      ['totalJobs', validators.isNumber],
      ['computedAt', validators.isString]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (data as any)[field];
      return validator(value);
    });
  },

  // Build Summary validation
  buildSummary: (data: any): data is import('../services/api').BuildSummaryDTO => {
    if (!validators.isObject(data)) return false;

    const required = [
      ['job_name', validators.isString],
      ['build_id', validators.isNumber],
      ['health_status', validators.isString],
      ['build_summary', validators.isString],
      ['build_started_time', validators.isString],
      ['build_duration', validators.isString],
      ['regression_detected', validators.isBoolean]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (data as any)[field];
      return validator(value);
    });
  },

  // Risk Score validation
  riskScore: (data: any): data is import('../services/api').RiskScoreDTO => {
    if (!validators.isObject(data)) return false;

    const required = [
      ['score', validators.isNumber],
      ['riskLevel', validators.isString]
    ] as const;

    const optional = [
      ['previousScore', validators.isOptional(validators.isNumber)],
      ['change', validators.isOptional(validators.isNumber)]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (data as any)[field];
      return validator(value);
    }) && optional.every(([field, validator]) => {
      const value = (data as any)[field];
      return validator(value);
    });
  },

  // Chart Data validation
  chartData: (data: any): data is import('../services/api').ChartDataDTO => {
    if (!validators.isObject(data)) return false;

    const required = [
      ['labels', validators.isArray],
      ['datasets', validators.isArray]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (data as any)[field];
      return validator(value);
    });
  },

  // Anomaly validation
  anomaly: (data: any): data is import('../services/api').AnomalyDTO => {
    if (!validators.isObject(data)) return false;

    const required = [
      ['type', validators.isString],
      ['severity', validators.isString],
      ['description', validators.isString]
    ] as const;

    const optional = [
      ['recommendation', validators.isOptional(validators.isString)],
      ['aiAnalysis', validators.isOptional(validators.isString)],
      ['details', validators.isOptional(validators.isObject)]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (data as any)[field];
      return validator(value);
    }) && optional.every(([field, validator]) => {
      const value = (data as any)[field];
      return validator(value);
    });
  },

  // AI Insights validation - supports both new and legacy formats
  aiInsights: (data: any): data is import('../services/api').AIInsightsDTO => {
    if (!validators.isObject(data)) return false;

    // New format fields (from actual API)
    const newFormat = [
      ['summary', validators.isOptional(validators.isString)],
      ['recommendations', validators.isOptional(validators.isArray)],
      ['trends', validators.isOptional(validators.isObject)]
    ] as const;

    // Legacy format fields (for backward compatibility)
    const legacyFormat = [
      ['criticalSecretExposure', validators.isOptional(validators.isString)],
      ['dependencyManagement', validators.isOptional(validators.isString)],
      ['securityTrendAlert', validators.isOptional(validators.isString)],
      ['recommendation', validators.isOptional((value: any) => validators.isString(value) || validators.isArray(value))] // ✅ FIX: Accept string OR array
    ] as const;

    // Check if it matches new format OR legacy format
    const hasNewFormat = newFormat.some(([field]) => field in data);
    const hasLegacyFormat = legacyFormat.some(([field]) => field in data);

    if (hasNewFormat) {
      // Validate new format
      return newFormat.every(([field, validator]) => {
        const value = (data as any)[field];
        return validator(value);
      });
    } else if (hasLegacyFormat) {
      // Validate legacy format
      return legacyFormat.every(([field, validator]) => {
        const value = (data as any)[field];
        return validator(value);
      });
    }

    // Empty object is also valid (no insights available)
    return Object.keys(data).length === 0;
  },

  // Job Explorer validation
  jobExplorer: (data: any): data is { job_name: string; last_build: string; status: string; anomalies: number } => {
    if (!validators.isObject(data)) return false;

    const required = [
      ['job_name', validators.isString],
      ['last_build', validators.isString],
      ['status', validators.isString],
      ['anomalies', validators.isNumber]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (data as any)[field];
      return validator(value);
    });
  },

  // Simple string array validation
  stringArray: (data: any): data is string[] => {
    return validators.isArray(data) && data.every(validators.isString);
  },

  // Generic response wrapper validation
  responseWrapper: <T>(dataValidator: (data: any) => data is T) => (response: any): response is { hasData: boolean; data?: T; message?: string } => {
    if (!validators.isObject(response)) return false;

    const required = [
      ['hasData', validators.isBoolean]
    ] as const;

    const optional = [
      ['data', validators.isOptional((data: any) => dataValidator(data))],
      ['message', validators.isOptional(validators.isString)]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (response as any)[field];
      return validator(value);
    }) && optional.every(([field, validator]) => {
      const value = (response as any)[field];
      return validator(value);
    });
  },

  // Build Summary specific response wrapper (uses hasAiData instead of hasData)
  buildSummaryWrapper: <T>(dataValidator: (data: any) => data is T) => (response: any): response is { hasAiData: boolean; data?: T; message?: string } => {
    if (!validators.isObject(response)) return false;

    const required = [
      ['hasAiData', validators.isBoolean]
    ] as const;

    const optional = [
      ['data', validators.isOptional((data: any) => dataValidator(data))],
      ['message', validators.isOptional(validators.isString)]
    ] as const;

    return required.every(([field, validator]) => {
      const value = (response as any)[field];
      return validator(value);
    }) && optional.every(([field, validator]) => {
      const value = (response as any)[field];
      return validator(value);
    });
  }
};

// Create validation schemas
export const validationSchemas = {
  recentJobBuilds: new ValidationSchema(apiValidators.recentJobBuild),
  securityAnomalies: new ValidationSchema(apiValidators.securityAnomalyCount),
  activeBuilds: new ValidationSchema(apiValidators.activeBuildCount),
  totalJobs: new ValidationSchema(apiValidators.jobCount)
};

// Utility function to safely validate API responses
export const validateApiResponse = <T>(
  data: any,
  validator: (data: any) => data is T,
  context: string
): T => {
  try {
    if (!validator(data)) {
      throw new ValidationError(`Invalid API response structure for ${context}`, context, data);
    }
    return data;
  } catch (error) {
    logger.error('API Response Validation Error', 'VALIDATION', {
      context,
      error: error instanceof Error ? error.message : error,
      receivedData: data,
      dataType: typeof data,
      isArray: Array.isArray(data)
    }, { component: 'ValidationUtils', action: 'validateApiResponse' });
    throw error;
  }
};

// Utility function to validate array responses
export const validateApiArrayResponse = <T>(
  data: any,
  validator: (data: any) => data is T,
  context: string
): T[] => {
  try {
    if (!validators.isArray(data)) {
      throw new ValidationError(`Expected array for ${context}`, context, data);
    }

    return data.map((item, index) => {
      if (!validator(item)) {
        throw new ValidationError(
          `Invalid item at index ${index} in ${context}`,
          `${context}[${index}]`,
          item
        );
      }
      return item;
    });
  } catch (error) {
    logger.error('API Array Response Validation Error', 'VALIDATION', {
      context,
      error: error instanceof Error ? error.message : error,
      receivedData: data,
      dataType: typeof data,
      arrayLength: Array.isArray(data) ? data.length : 'not an array'
    }, { component: 'ValidationUtils', action: 'validateApiArrayResponse' });
    throw error;
  }
};

// Safe API call wrapper with validation
export const safeApiCall = async <T>(
  apiCall: () => Promise<any>,
  validator: (data: any) => data is T,
  context: string,
  fallbackValue?: T
): Promise<T> => {
  try {
    const response = await apiCall();
    return validateApiResponse(response, validator, context);
  } catch (error) {
    if (error instanceof ValidationError) {
      logger.error('API validation failed', 'VALIDATION', {
        context,
        error: error.message,
        field: error.field,
        receivedData: error.received,
        hasFallback: fallbackValue !== undefined
      }, { component: 'ValidationUtils', action: 'safeApiCall' });

      if (fallbackValue !== undefined) {
        logger.warn('Using fallback value for failed validation', 'VALIDATION', {
          context,
          fallbackValue
        }, { component: 'ValidationUtils', action: 'safeApiCall' });
        return fallbackValue;
      }
    } else {
      logger.error('API call failed', 'VALIDATION', {
        context,
        error: error instanceof Error ? error.message : error
      }, { component: 'ValidationUtils', action: 'safeApiCall' });
    }
    throw error;
  }
};
