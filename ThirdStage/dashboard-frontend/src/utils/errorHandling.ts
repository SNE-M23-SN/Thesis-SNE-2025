// Standardized error handling utilities

import { ERROR_MESSAGES } from '../constants/dashboard';

// Error types for better categorization
export enum ErrorType {
  NETWORK = 'NETWORK',
  VALIDATION = 'VALIDATION',
  AUTHENTICATION = 'AUTHENTICATION',
  AUTHORIZATION = 'AUTHORIZATION',
  NOT_FOUND = 'NOT_FOUND',
  SERVER_ERROR = 'SERVER_ERROR',
  UNKNOWN = 'UNKNOWN'
}

// Error severity levels
export enum ErrorSeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

// Standardized error interface
export interface AppError {
  type: ErrorType;
  severity: ErrorSeverity;
  message: string;
  userMessage: string;
  code?: string | number;
  details?: any;
  timestamp: Date;
  context?: string;
  retryable: boolean;
}

// Error factory functions
export const createError = (
  type: ErrorType,
  severity: ErrorSeverity,
  message: string,
  userMessage: string,
  options: {
    code?: string | number;
    details?: any;
    context?: string;
    retryable?: boolean;
  } = {}
): AppError => ({
  type,
  severity,
  message,
  userMessage,
  code: options.code,
  details: options.details,
  timestamp: new Date(),
  context: options.context,
  retryable: options.retryable ?? false
});

// Network error factory
export const createNetworkError = (
  message: string,
  userMessage: string = 'Network connection failed. Please check your internet connection.',
  options: { code?: number; retryable?: boolean } = {}
): AppError => createError(
  ErrorType.NETWORK,
  ErrorSeverity.HIGH,
  message,
  userMessage,
  {
    code: options.code,
    retryable: options.retryable ?? true
  }
);

// API error factory
export const createApiError = (
  status: number,
  message: string,
  details?: any
): AppError => {
  let type: ErrorType;
  let severity: ErrorSeverity;
  let userMessage: string;
  let retryable = false;

  switch (status) {
    case 400:
      type = ErrorType.VALIDATION;
      severity = ErrorSeverity.MEDIUM;
      userMessage = 'Invalid request. Please check your input and try again.';
      break;
    case 401:
      type = ErrorType.AUTHENTICATION;
      severity = ErrorSeverity.HIGH;
      userMessage = 'Authentication required. Please log in again.';
      break;
    case 403:
      type = ErrorType.AUTHORIZATION;
      severity = ErrorSeverity.HIGH;
      userMessage = 'Access denied. You do not have permission to perform this action.';
      break;
    case 404:
      type = ErrorType.NOT_FOUND;
      severity = ErrorSeverity.MEDIUM;
      userMessage = 'The requested resource was not found.';
      break;
    case 429:
      type = ErrorType.NETWORK;
      severity = ErrorSeverity.MEDIUM;
      userMessage = 'Too many requests. Please wait a moment and try again.';
      retryable = true;
      break;
    case 500:
    case 502:
    case 503:
    case 504:
      type = ErrorType.SERVER_ERROR;
      severity = ErrorSeverity.HIGH;
      userMessage = 'Server error occurred. Please try again later.';
      retryable = true;
      break;
    default:
      type = ErrorType.UNKNOWN;
      severity = ErrorSeverity.MEDIUM;
      userMessage = 'An unexpected error occurred. Please try again.';
      retryable = true;
  }

  return createError(type, severity, message, userMessage, {
    code: status,
    details,
    retryable
  });
};

// Error parser for different error sources
export const parseError = (error: any, context?: string): AppError => {
  // Axios error
  if (error.response) {
    return createApiError(
      error.response.status,
      error.response.data?.message || error.message,
      error.response.data
    );
  }

  // Network error
  if (error.request) {
    return createNetworkError(
      'Network request failed',
      'Unable to connect to the server. Please check your internet connection.',
      { retryable: true }
    );
  }

  // Validation error
  if (error.name === 'ValidationError') {
    return createError(
      ErrorType.VALIDATION,
      ErrorSeverity.MEDIUM,
      error.message,
      'Data validation failed. Please check your input.',
      { details: error.details, context }
    );
  }

  // Generic error
  return createError(
    ErrorType.UNKNOWN,
    ErrorSeverity.MEDIUM,
    error.message || 'Unknown error occurred',
    'An unexpected error occurred. Please try again.',
    { context, retryable: true }
  );
};

// Error logging utility
export const logError = (error: AppError, additionalContext?: any): void => {
  const logData = {
    ...error,
    additionalContext,
    userAgent: navigator.userAgent,
    url: window.location.href
  };

  // Log to console in development
  if (process.env.NODE_ENV === 'development') {
    console.group(`🚨 ${error.severity} Error - ${error.type}`);
    console.error('Message:', error.message);
    console.error('User Message:', error.userMessage);
    console.error('Context:', error.context);
    console.error('Details:', error.details);
    console.error('Full Error:', logData);
    console.groupEnd();
  }

  // In production, send to error tracking service
  // Example: Sentry.captureException(error, { extra: logData });
};

// Retry utility for retryable errors
export const withRetry = async <T>(
  operation: () => Promise<T>,
  maxRetries: number = 3,
  delay: number = 1000,
  context?: string
): Promise<T> => {
  let lastError: AppError;

  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await operation();
    } catch (error) {
      lastError = parseError(error, context);
      
      // Don't retry if error is not retryable
      if (!lastError.retryable || attempt === maxRetries) {
        logError(lastError, { attempt, maxRetries });
        throw lastError;
      }

      // Wait before retrying
      await new Promise(resolve => setTimeout(resolve, delay * attempt));
      
      console.warn(`Retry attempt ${attempt}/${maxRetries} for ${context || 'operation'}`);
    }
  }

  throw lastError!;
};

// Error boundary helper
export const handleComponentError = (error: Error, errorInfo: any): AppError => {
  const appError = createError(
    ErrorType.UNKNOWN,
    ErrorSeverity.CRITICAL,
    error.message,
    'A component error occurred. Please refresh the page.',
    {
      details: {
        stack: error.stack,
        componentStack: errorInfo.componentStack
      },
      context: 'React Component',
      retryable: false
    }
  );

  logError(appError, errorInfo);
  return appError;
};

// User-friendly error messages
export const getUserFriendlyMessage = (error: AppError): string => {
  // Use predefined messages from constants when available
  const predefinedMessages: Record<string, string> = {
    'dashboard_load_failed': ERROR_MESSAGES.DASHBOARD_LOAD_FAILED,
    'api_connection_error': ERROR_MESSAGES.API_CONNECTION_ERROR,
    'refresh_failed': ERROR_MESSAGES.REFRESH_FAILED,
    'unknown_error': ERROR_MESSAGES.UNKNOWN_ERROR
  };

  // Check if we have a predefined message
  const predefined = Object.values(predefinedMessages).find(msg => 
    error.userMessage.includes(msg) || error.message.includes(msg)
  );

  if (predefined) {
    return predefined;
  }

  // Return the user message from the error
  return error.userMessage;
};

// Error recovery suggestions
export const getRecoverySuggestions = (error: AppError): string[] => {
  const suggestions: string[] = [];

  switch (error.type) {
    case ErrorType.NETWORK:
      suggestions.push('Check your internet connection');
      suggestions.push('Try refreshing the page');
      if (error.retryable) {
        suggestions.push('Wait a moment and try again');
      }
      break;

    case ErrorType.AUTHENTICATION:
      suggestions.push('Log in again');
      suggestions.push('Clear your browser cache');
      break;

    case ErrorType.AUTHORIZATION:
      suggestions.push('Contact your administrator for access');
      suggestions.push('Verify you have the necessary permissions');
      break;

    case ErrorType.NOT_FOUND:
      suggestions.push('Check the URL is correct');
      suggestions.push('Try navigating from the main page');
      break;

    case ErrorType.SERVER_ERROR:
      suggestions.push('Try again in a few minutes');
      suggestions.push('Contact support if the problem persists');
      break;

    case ErrorType.VALIDATION:
      suggestions.push('Check your input data');
      suggestions.push('Ensure all required fields are filled');
      break;

    default:
      suggestions.push('Refresh the page');
      suggestions.push('Try again later');
      suggestions.push('Contact support if the problem persists');
  }

  return suggestions;
};

export default {
  createError,
  createNetworkError,
  createApiError,
  parseError,
  logError,
  withRetry,
  handleComponentError,
  getUserFriendlyMessage,
  getRecoverySuggestions
};
