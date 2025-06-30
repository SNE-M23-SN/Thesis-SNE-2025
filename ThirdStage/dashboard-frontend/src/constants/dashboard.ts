// Dashboard Configuration Constants

// Auto-refresh settings
export const REFRESH_INTERVALS = {
  AUTO_REFRESH: 30000, // 30 seconds - updates every 30s regardless of user activity
  USER_INTERACTION_TIMEOUT: 2000, // 2 seconds - only for UI feedback, doesn't block updates
  SCROLL_RESTORE_DELAY: 50, // 50ms
} as const;

// Time range mappings for API calls
// For Total Jobs API (uses underscore format)
export const TIME_RANGE_MAP = {
  '7 days': '7_days',
  '14 days': '14_days',
  '30 days': '30_days',
  '60 days': '60_days',
  '180 days': '180_days',
  'all time': 'all_time'
} as const;

// For Security Anomalies API (uses space format)
export const SECURITY_TIME_RANGE_MAP = {
  '7 days': '7 days',
  '14 days': '14 days',
  '30 days': '30 days',
  '60 days': '60 days',
  '180 days': '180 days',
  'all time': 'all time'
} as const;

// API retry settings
export const API_CONFIG = {
  MAX_RETRIES: 3,
  RETRY_DELAY: 1000, // 1 second
  TIMEOUT: 10000, // 10 seconds
} as const;

// UI Constants
export const UI_CONSTANTS = {
  SIDEBAR_TRANSITION_DURATION: 300, // ms
  NOTIFICATION_DURATION: 5000, // 5 seconds
  LOADING_SPINNER_SIZE: 12, // rem
  CHART_HEIGHT: 180, // px
  GAUGE_HEIGHT: 150, // px
} as const;

// Error Messages
export const ERROR_MESSAGES = {
  DASHBOARD_LOAD_FAILED: 'Failed to load dashboard data',
  API_CONNECTION_ERROR: 'Please check your API connection',
  REFRESH_FAILED: 'Failed to refresh dashboard. Please try again.',
  PARTIAL_LOAD_WARNING: 'Some data may be outdated',
  UNKNOWN_ERROR: 'An unexpected error occurred',
} as const;

// Success Messages
export const SUCCESS_MESSAGES = {
  DASHBOARD_REFRESHED: 'Dashboard refreshed successfully',
  BUILD_RERUN_SUCCESS: 'Build rerun initiated successfully',
  DATA_COPIED: 'Data copied to clipboard',
} as const;

// Log sanitization patterns
export const LOG_SANITIZATION = {
  DANGEROUS_TAGS: [
    /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
    /<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi,
    /<object\b[^<]*(?:(?!<\/object>)<[^<]*)*<\/object>/gi,
    /<embed\b[^<]*(?:(?!<\/embed>)<[^<]*)*<\/embed>/gi,
    /<link\b[^>]*>/gi,
    /<meta\b[^>]*>/gi,
  ],
  DANGEROUS_PROTOCOLS: [
    /javascript:/gi,
    /data:/gi,
    /vbscript:/gi,
  ],
  REPLACEMENT_TEXT: {
    SCRIPT: '[SCRIPT_REMOVED]',
    IFRAME: '[IFRAME_REMOVED]',
    OBJECT: '[OBJECT_REMOVED]',
    EMBED: '[EMBED_REMOVED]',
    LINK: '[LINK_REMOVED]',
    META: '[META_REMOVED]',
    JAVASCRIPT: 'javascript_removed:',
    DATA: 'data_removed:',
    VBSCRIPT: 'vbscript_removed:',
  },
} as const;

// Component size limits
export const COMPONENT_LIMITS = {
  MAX_RECENT_JOBS_SIDEBAR: 4,
  MAX_RECENT_BUILDS_DISPLAY: 3,
  MAX_LOG_ENTRIES_PER_PAGE: 50,
  MAX_ANOMALIES_PER_PAGE: 10,
} as const;

// Chart configuration
export const CHART_CONFIG = {
  COLORS: {
    LOW: '#FF6384',
    HIGH: '#36A2EB', 
    MEDIUM: '#FFCE56',
    WARNING: '#4BC0C0',
    CRITICAL: '#9966FF',
  },
  ANIMATION_DURATION: 750,
  RESPONSIVE: true,
} as const;

// Accessibility constants
export const A11Y_CONSTANTS = {
  ARIA_LABELS: {
    REFRESH_BUTTON: 'Refresh dashboard data',
    THEME_TOGGLE: 'Toggle dark mode',
    SIDEBAR_TOGGLE: 'Toggle sidebar',
    CLOSE_MODAL: 'Close modal',
    SEARCH_INPUT: 'Search builds or jobs',
  },
  KEYBOARD_SHORTCUTS: {
    ESCAPE: 'Escape',
    ENTER: 'Enter',
    SPACE: ' ',
  },
} as const;

export type TimeRangeKey = keyof typeof TIME_RANGE_MAP;
export type ErrorMessageKey = keyof typeof ERROR_MESSAGES;
export type SuccessMessageKey = keyof typeof SUCCESS_MESSAGES;
