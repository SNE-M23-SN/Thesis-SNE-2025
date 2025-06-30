// Utils index file for easier imports

// Re-export everything from errorHandling
export {
  ErrorType,
  ErrorSeverity,
  createError,
  createNetworkError,
  createApiError,
  parseError,
  logError,
  withRetry,
  handleComponentError,
  getUserFriendlyMessage,
  getRecoverySuggestions
} from './errorHandling';

export type { AppError } from './errorHandling';

// Re-export everything from accessibility
export {
  ARIA_LABELS,
  ARIA_DESCRIPTIONS,
  KEYBOARD_KEYS,
  focusManagement,
  screenReader,
  colorContrast,
  a11yValidation,
  useKeyboardNavigation,
  generateA11yId
} from './accessibility';

export type { AriaLabelKey, AriaDescriptionKey } from './accessibility';

// Re-export everything from validation
export {
  ValidationError,
  validators,
  ValidationSchema,
  apiValidators,
  validationSchemas,
  validateApiResponse,
  validateApiArrayResponse,
  safeApiCall
} from './validation';

// Re-export logging system
export { logger, LogLevel } from './logger';
export type { LogEntry, LogContext } from './logger';
