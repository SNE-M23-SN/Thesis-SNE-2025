// Professional logging system for production-grade applications

import { logError, type AppError } from './errorHandling';

// Log levels with numeric values for filtering
export enum LogLevel {
  DEBUG = 0,
  INFO = 1,
  WARN = 2,
  ERROR = 3
}

// Structured log entry interface
export interface LogEntry {
  timestamp: string;
  level: keyof typeof LogLevel;
  message: string;
  context?: string;
  data?: any;
  userId?: string;
  sessionId?: string;
  requestId?: string;
  component?: string;
  action?: string;
}

// Log context for additional metadata
export interface LogContext {
  component?: string;
  action?: string;
  userId?: string;
  sessionId?: string;
  requestId?: string;
  [key: string]: any;
}

// Production-grade logger class
class Logger {
  private isDevelopment = process.env.NODE_ENV === 'development';
  private logLevel = this.isDevelopment ? LogLevel.DEBUG : LogLevel.WARN;
  private sessionId = this.generateSessionId();

  constructor() {
    // Initialize session tracking
    this.info('Logger initialized', 'LOGGER', {
      environment: process.env.NODE_ENV,
      logLevel: LogLevel[this.logLevel],
      sessionId: this.sessionId
    });
  }

  // Generate unique session ID
  private generateSessionId(): string {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  // Core logging method
  private log(level: LogLevel, message: string, context?: string, data?: any, additionalContext?: LogContext): void {
    // Filter logs based on current log level
    if (level < this.logLevel) {
      return;
    }

    const logEntry: LogEntry = {
      timestamp: new Date().toISOString(),
      level: LogLevel[level] as keyof typeof LogLevel,
      message,
      context,
      data,
      sessionId: this.sessionId,
      ...additionalContext
    };

    // Development logging - structured console output
    if (this.isDevelopment) {
      this.logToConsole(logEntry);
    }

    // Production logging - send to monitoring service
    if (!this.isDevelopment && level >= LogLevel.WARN) {
      this.sendToMonitoring(logEntry);
    }

    // Store in session storage for debugging (development only)
    if (this.isDevelopment) {
      this.storeInSession(logEntry);
    }
  }

  // Console logging with proper formatting
  private logToConsole(entry: LogEntry): void {
    const emoji = this.getLevelEmoji(entry.level);
    const timestamp = new Date(entry.timestamp).toLocaleTimeString();
    
    const logMethod = this.getConsoleMethod(entry.level);
    const contextStr = entry.context ? ` [${entry.context}]` : '';
    const componentStr = entry.component ? ` {${entry.component}}` : '';
    
    logMethod(`${emoji} ${timestamp}${contextStr}${componentStr} ${entry.message}`);
    
    if (entry.data) {
      console.groupCollapsed('📊 Data');
      console.log(entry.data);
      console.groupEnd();
    }
  }

  // Get appropriate console method for log level
  private getConsoleMethod(level: keyof typeof LogLevel): (...args: any[]) => void {
    switch (level) {
      case 'DEBUG': return console.debug;
      case 'INFO': return console.info;
      case 'WARN': return console.warn;
      case 'ERROR': return console.error;
      default: return console.log;
    }
  }

  // Get emoji for log level
  private getLevelEmoji(level: keyof typeof LogLevel): string {
    switch (level) {
      case 'DEBUG': return '🔍';
      case 'INFO': return 'ℹ️';
      case 'WARN': return '⚠️';
      case 'ERROR': return '❌';
      default: return '📝';
    }
  }

  // Store logs in session storage for debugging
  private storeInSession(entry: LogEntry): void {
    try {
      const logs = JSON.parse(sessionStorage.getItem('dashboard_logs') || '[]');
      logs.push(entry);
      
      // Keep only last 100 logs to prevent memory issues
      if (logs.length > 100) {
        logs.splice(0, logs.length - 100);
      }
      
      sessionStorage.setItem('dashboard_logs', JSON.stringify(logs));
    } catch (error) {
      // Silently fail if session storage is not available
    }
  }

  // Send logs to monitoring service (production)
  private sendToMonitoring(entry: LogEntry): void {
    // In production, this would integrate with services like:
    // - Sentry for error tracking
    // - LogRocket for session replay
    // - DataDog for application monitoring
    // - Custom analytics endpoint
    
    try {
      // Example integration point
      if (window.gtag) {
        window.gtag('event', 'log_entry', {
          event_category: 'logging',
          event_label: entry.level,
          custom_parameter_level: entry.level,
          custom_parameter_context: entry.context,
          custom_parameter_message: entry.message
        });
      }
      
      // Example: Send to custom monitoring endpoint
      // fetch('/api/logs', {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify(entry)
      // }).catch(() => {}); // Silent fail for logging
      
    } catch (error) {
      // Never throw errors from logging system
    }
  }

  // Public logging methods
  debug(message: string, context?: string, data?: any, additionalContext?: LogContext): void {
    this.log(LogLevel.DEBUG, message, context, data, additionalContext);
  }

  info(message: string, context?: string, data?: any, additionalContext?: LogContext): void {
    this.log(LogLevel.INFO, message, context, data, additionalContext);
  }

  warn(message: string, context?: string, data?: any, additionalContext?: LogContext): void {
    this.log(LogLevel.WARN, message, context, data, additionalContext);
  }

  error(message: string, context?: string, data?: any, additionalContext?: LogContext): void {
    this.log(LogLevel.ERROR, message, context, data, additionalContext);
  }

  // Enhanced error logging that integrates with Phase 2 error handling
  logAppError(error: AppError, context?: string, additionalContext?: LogContext): void {
    // Use existing Phase 2 error handling (PRESERVED)
    logError(error, additionalContext);
    
    // Add structured logging for production monitoring
    this.error(error.userMessage, context, {
      errorType: error.type,
      severity: error.severity,
      technicalMessage: error.message,
      retryable: error.retryable,
      errorDetails: error.details,
      errorContext: error.context
    }, additionalContext);
  }

  // API operation logging
  logApiOperation(operation: string, endpoint: string, duration?: number, success: boolean = true, data?: any): void {
    const level = success ? LogLevel.DEBUG : LogLevel.WARN;
    const message = `API ${operation} ${success ? 'completed' : 'failed'}: ${endpoint}`;
    
    this.log(level, message, 'API', {
      operation,
      endpoint,
      duration,
      success,
      data: success ? undefined : data // Only log data on failures
    });
  }

  // Component lifecycle logging
  logComponentEvent(component: string, event: string, data?: any): void {
    this.debug(`Component ${event}`, 'COMPONENT', data, { component, action: event });
  }

  // Performance logging
  logPerformance(operation: string, duration: number, context?: string): void {
    const level = duration > 1000 ? LogLevel.WARN : LogLevel.DEBUG;
    const message = `Performance: ${operation} took ${duration}ms`;
    
    this.log(level, message, context || 'PERFORMANCE', {
      operation,
      duration,
      threshold: duration > 1000 ? 'SLOW' : 'NORMAL'
    });
  }

  // User interaction logging
  logUserAction(action: string, component?: string, data?: any): void {
    this.info(`User action: ${action}`, 'USER_INTERACTION', data, {
      component,
      action
    });
  }

  // Get session logs for debugging
  getSessionLogs(): LogEntry[] {
    try {
      return JSON.parse(sessionStorage.getItem('dashboard_logs') || '[]');
    } catch {
      return [];
    }
  }

  // Clear session logs
  clearSessionLogs(): void {
    try {
      sessionStorage.removeItem('dashboard_logs');
      this.info('Session logs cleared', 'LOGGER');
    } catch {
      // Silent fail
    }
  }

  // Set log level dynamically (for debugging)
  setLogLevel(level: LogLevel): void {
    this.logLevel = level;
    this.info(`Log level changed to ${LogLevel[level]}`, 'LOGGER', { newLevel: level });
  }
}

// Create singleton logger instance
export const logger = new Logger();

// Extend window object for debugging access
declare global {
  interface Window {
    dashboardLogger?: Logger;
    gtag?: (...args: any[]) => void;
  }
}

// Make logger available globally in development
if (process.env.NODE_ENV === 'development') {
  window.dashboardLogger = logger;
}

export default logger;
