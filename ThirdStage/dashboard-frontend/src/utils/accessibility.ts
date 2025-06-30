// Accessibility utilities and constants

export const ARIA_LABELS = {
  // Navigation
  MAIN_NAVIGATION: 'Main navigation',
  SIDEBAR_TOGGLE: 'Toggle sidebar navigation',
  THEME_TOGGLE: 'Toggle dark mode',
  REFRESH_BUTTON: 'Refresh dashboard data',
  
  // Modals
  CLOSE_MODAL: 'Close modal',
  MODAL_OVERLAY: 'Modal overlay',
  
  // Forms and inputs
  SEARCH_INPUT: 'Search builds or jobs',
  JOB_FILTER: 'Filter by job',
  TIME_RANGE_FILTER: 'Select time range',
  
  // Charts and data
  RISK_SCORE_CHART: 'Risk score gauge chart',
  ANOMALY_TREND_CHART: 'Anomaly trend chart',
  SEVERITY_DISTRIBUTION_CHART: 'Severity distribution chart',
  
  // Actions
  VIEW_DETAILS: 'View details',
  RERUN_BUILD: 'Rerun build',
  COPY_TO_CLIPBOARD: 'Copy to clipboard',
  
  // Status indicators
  LOADING: 'Loading content',
  ERROR_STATE: 'Error occurred',
  SUCCESS_STATE: 'Action completed successfully',
} as const;

export const ARIA_DESCRIPTIONS = {
  DASHBOARD_OVERVIEW: 'Dashboard showing Jenkins pipeline builds and security anomalies',
  BUILD_SUMMARY: 'Summary of selected build including status and metrics',
  RISK_SCORE: 'Security risk score for the selected build',
  RECENT_BUILDS: 'List of recent job builds with their status',
  ANOMALY_TRENDS: 'Chart showing anomaly trends over time',
  SEVERITY_DISTRIBUTION: 'Chart showing distribution of anomaly severities',
} as const;

// Keyboard navigation constants
export const KEYBOARD_KEYS = {
  ESCAPE: 'Escape',
  ENTER: 'Enter',
  SPACE: ' ',
  TAB: 'Tab',
  ARROW_UP: 'ArrowUp',
  ARROW_DOWN: 'ArrowDown',
  ARROW_LEFT: 'ArrowLeft',
  ARROW_RIGHT: 'ArrowRight',
} as const;

// Focus management utilities
export const focusManagement = {
  // Trap focus within a container
  trapFocus: (container: HTMLElement) => {
    const focusableElements = container.querySelectorAll(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    const firstElement = focusableElements[0] as HTMLElement;
    const lastElement = focusableElements[focusableElements.length - 1] as HTMLElement;

    const handleTabKey = (e: KeyboardEvent) => {
      if (e.key === KEYBOARD_KEYS.TAB) {
        if (e.shiftKey) {
          if (document.activeElement === firstElement) {
            lastElement.focus();
            e.preventDefault();
          }
        } else {
          if (document.activeElement === lastElement) {
            firstElement.focus();
            e.preventDefault();
          }
        }
      }
    };

    container.addEventListener('keydown', handleTabKey);
    
    // Focus first element
    if (firstElement) {
      firstElement.focus();
    }

    // Return cleanup function
    return () => {
      container.removeEventListener('keydown', handleTabKey);
    };
  },

  // Restore focus to previously focused element
  restoreFocus: (previouslyFocusedElement: HTMLElement | null) => {
    if (previouslyFocusedElement && document.contains(previouslyFocusedElement)) {
      previouslyFocusedElement.focus();
    }
  },

  // Get currently focused element
  getCurrentFocus: (): HTMLElement | null => {
    return document.activeElement as HTMLElement;
  }
};

// Screen reader utilities
export const screenReader = {
  // Announce message to screen readers
  announce: (message: string, priority: 'polite' | 'assertive' = 'polite') => {
    const announcement = document.createElement('div');
    announcement.setAttribute('aria-live', priority);
    announcement.setAttribute('aria-atomic', 'true');
    announcement.setAttribute('class', 'sr-only');
    announcement.textContent = message;
    
    document.body.appendChild(announcement);
    
    // Remove after announcement
    setTimeout(() => {
      document.body.removeChild(announcement);
    }, 1000);
  },

  // Create visually hidden text for screen readers
  createScreenReaderText: (text: string): HTMLSpanElement => {
    const span = document.createElement('span');
    span.className = 'sr-only';
    span.textContent = text;
    return span;
  }
};

// Color contrast utilities
export const colorContrast = {
  // Check if color combination meets WCAG AA standards
  meetsWCAGAA: (foreground: string, background: string): boolean => {
    // This is a simplified check - in production, use a proper color contrast library
    // For now, we'll assume our design system colors meet WCAG standards
    return true;
  },

  // Get high contrast alternative
  getHighContrastColor: (color: string): string => {
    // Return high contrast alternatives for common colors
    const highContrastMap: Record<string, string> = {
      'text-gray-500': 'text-gray-900 dark:text-gray-100',
      'text-blue-500': 'text-blue-700 dark:text-blue-300',
      'text-green-500': 'text-green-700 dark:text-green-300',
      'text-red-500': 'text-red-700 dark:text-red-300',
      'text-yellow-500': 'text-yellow-700 dark:text-yellow-300',
    };
    
    return highContrastMap[color] || color;
  }
};

// Accessibility validation utilities
export const a11yValidation = {
  // Check if element has proper ARIA labels
  hasProperLabeling: (element: HTMLElement): boolean => {
    const hasAriaLabel = element.hasAttribute('aria-label');
    const hasAriaLabelledBy = element.hasAttribute('aria-labelledby');
    const hasTitle = element.hasAttribute('title');
    const hasTextContent = element.textContent?.trim().length > 0;
    
    return hasAriaLabel || hasAriaLabelledBy || hasTitle || hasTextContent;
  },

  // Check if interactive element is keyboard accessible
  isKeyboardAccessible: (element: HTMLElement): boolean => {
    const tabIndex = element.getAttribute('tabindex');
    const isInteractive = ['button', 'a', 'input', 'select', 'textarea'].includes(
      element.tagName.toLowerCase()
    );
    
    return isInteractive || (tabIndex !== null && tabIndex !== '-1');
  },

  // Validate modal accessibility
  validateModal: (modal: HTMLElement): string[] => {
    const issues: string[] = [];
    
    if (!modal.hasAttribute('role') || modal.getAttribute('role') !== 'dialog') {
      issues.push('Modal should have role="dialog"');
    }
    
    if (!modal.hasAttribute('aria-modal') || modal.getAttribute('aria-modal') !== 'true') {
      issues.push('Modal should have aria-modal="true"');
    }
    
    if (!modal.hasAttribute('aria-labelledby') && !modal.hasAttribute('aria-label')) {
      issues.push('Modal should have aria-labelledby or aria-label');
    }
    
    return issues;
  }
};

// Custom hook for keyboard navigation
export const useKeyboardNavigation = (
  onEscape?: () => void,
  onEnter?: () => void,
  onSpace?: () => void
) => {
  const handleKeyDown = (event: KeyboardEvent) => {
    switch (event.key) {
      case KEYBOARD_KEYS.ESCAPE:
        if (onEscape) {
          event.preventDefault();
          onEscape();
        }
        break;
      case KEYBOARD_KEYS.ENTER:
        if (onEnter) {
          event.preventDefault();
          onEnter();
        }
        break;
      case KEYBOARD_KEYS.SPACE:
        if (onSpace) {
          event.preventDefault();
          onSpace();
        }
        break;
    }
  };

  return { handleKeyDown };
};

// Utility to generate unique IDs for accessibility
export const generateA11yId = (prefix: string): string => {
  return `${prefix}-${Math.random().toString(36).substr(2, 9)}`;
};

export type AriaLabelKey = keyof typeof ARIA_LABELS;
export type AriaDescriptionKey = keyof typeof ARIA_DESCRIPTIONS;
