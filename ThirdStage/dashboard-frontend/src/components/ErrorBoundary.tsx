import React, { Component, ErrorInfo, ReactNode } from 'react';
import {
  handleComponentError,
  getUserFriendlyMessage,
  getRecoverySuggestions,
  type AppError
} from '../utils';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
}

interface State {
  hasError: boolean;
  error?: Error;
  errorInfo?: ErrorInfo;
  appError?: AppError;
}

class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    // Update state so the next render will show the fallback UI
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // Create standardized app error
    const appError = handleComponentError(error, errorInfo);

    // Update state with error info
    this.setState({
      error,
      errorInfo,
      appError
    });

    // Call optional error handler
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: undefined, errorInfo: undefined, appError: undefined });
  };

  render() {
    if (this.state.hasError) {
      // Custom fallback UI
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // Default fallback UI with standardized error handling
      const userMessage = this.state.appError ? getUserFriendlyMessage(this.state.appError) : 'An unexpected error occurred in the dashboard. Please try refreshing the page.';
      const suggestions = this.state.appError ? getRecoverySuggestions(this.state.appError) : ['Refresh the page', 'Try again later'];

      return (
        <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-8 max-w-md w-full text-center">
            <div className="mb-6">
              <i className="fas fa-exclamation-triangle text-4xl text-red-500 mb-4"></i>
              <h2 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-2">
                Something went wrong
              </h2>
              <p className="text-gray-600 dark:text-gray-400 text-sm">
                {userMessage}
              </p>
            </div>

            {/* Recovery suggestions */}
            <div className="mb-6 text-left">
              <h3 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">
                Try these solutions:
              </h3>
              <ul className="text-xs text-gray-600 dark:text-gray-400 space-y-1">
                {suggestions.map((suggestion, index) => (
                  <li key={index} className="flex items-start">
                    <span className="text-blue-500 mr-2">•</span>
                    {suggestion}
                  </li>
                ))}
              </ul>
            </div>

            {/* Error details in development */}
            {process.env.NODE_ENV === 'development' && this.state.error && (
              <div className="mb-6 text-left">
                <details className="bg-gray-100 dark:bg-gray-700 p-4 rounded text-xs">
                  <summary className="cursor-pointer font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Error Details (Development Only)
                  </summary>
                  <div className="mt-2 space-y-2">
                    <div>
                      <strong>Error:</strong>
                      <pre className="mt-1 text-red-600 dark:text-red-400 whitespace-pre-wrap">
                        {this.state.error.toString()}
                      </pre>
                    </div>
                    {this.state.errorInfo && (
                      <div>
                        <strong>Component Stack:</strong>
                        <pre className="mt-1 text-gray-600 dark:text-gray-400 whitespace-pre-wrap">
                          {this.state.errorInfo.componentStack}
                        </pre>
                      </div>
                    )}
                  </div>
                </details>
              </div>
            )}

            <div className="flex space-x-3 justify-center">
              <button
                onClick={this.handleRetry}
                className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded text-sm transition"
              >
                Try Again
              </button>
              <button
                onClick={() => window.location.reload()}
                className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded text-sm transition"
              >
                Refresh Page
              </button>
            </div>

            <div className="mt-6 pt-4 border-t border-gray-200 dark:border-gray-700">
              <p className="text-xs text-gray-500 dark:text-gray-400">
                If this problem persists, please contact the development team.
              </p>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;

// Higher-order component for easier usage
export const withErrorBoundary = <P extends object>(
  Component: React.ComponentType<P>,
  fallback?: ReactNode,
  onError?: (error: Error, errorInfo: ErrorInfo) => void
) => {
  const WrappedComponent = (props: P) => (
    <ErrorBoundary fallback={fallback} onError={onError}>
      <Component {...props} />
    </ErrorBoundary>
  );

  WrappedComponent.displayName = `withErrorBoundary(${Component.displayName || Component.name})`;
  return WrappedComponent;
};

// Hook for error reporting in functional components
export const useErrorHandler = () => {
  return (error: Error, errorInfo?: any) => {
    console.error('Manual error report:', error, errorInfo);
    
    // In production, report to error tracking service
    // Example: Sentry.captureException(error, { extra: errorInfo });
    
    // For now, we'll throw the error to trigger the error boundary
    throw error;
  };
};
