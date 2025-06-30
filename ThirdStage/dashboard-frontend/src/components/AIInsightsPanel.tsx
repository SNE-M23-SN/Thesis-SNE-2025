import React, { useState, useEffect } from 'react';
import { dashboardApi } from '../services/api';
import type { AIInsightsDTO, RecentJobBuildDTO } from '../services/api';
import { logger } from '../utils';

interface AIInsight {
  type: 'info' | 'warning' | 'error' | 'success';
  title: string;
  message: string;
}

interface AIInsightsPanelProps {
  selectedBuild?: RecentJobBuildDTO | null;
  hasBuilds?: boolean;
}

const AIInsightsPanel: React.FC<AIInsightsPanelProps> = ({ selectedBuild, hasBuilds = true }) => {
  const [aiInsights, setAiInsights] = useState<AIInsightsDTO | null>(null);
  const [loading, setLoading] = useState(false);

  // Simple Markdown renderer for AI insights
  const renderMarkdown = (text: string): React.ReactElement => {
    // Split text into lines for processing
    const lines = text.split('\n');
    const elements: React.ReactElement[] = [];

    lines.forEach((line, index) => {
      // Handle numbered lists (1. 2. 3. etc.)
      if (/^\d+\.\s+/.test(line)) {
        const content = line.replace(/^\d+\.\s+/, '');
        const renderedContent = renderInlineMarkdown(content);
        elements.push(
          <div key={index} className="mb-2 pl-4">
            <span className="font-medium text-blue-600 dark:text-blue-400 mr-2">
              {line.match(/^\d+\./)?.[0]}
            </span>
            {renderedContent}
          </div>
        );
      } else if (line.trim() === '') {
        // Empty line - add spacing
        elements.push(<div key={index} className="mb-2"></div>);
      } else {
        // Regular line
        const renderedContent = renderInlineMarkdown(line);
        elements.push(
          <div key={index} className="mb-1">
            {renderedContent}
          </div>
        );
      }
    });

    return <div>{elements}</div>;
  };

  // Render inline markdown (bold, code, etc.)
  const renderInlineMarkdown = (text: string): React.ReactElement => {
    const parts: (string | React.ReactElement)[] = [];
    let currentIndex = 0;

    // Handle **bold** text
    const boldRegex = /\*\*(.*?)\*\*/g;
    let match;

    while ((match = boldRegex.exec(text)) !== null) {
      // Add text before the match
      if (match.index > currentIndex) {
        const beforeText = text.slice(currentIndex, match.index);
        parts.push(...renderCodeAndBackticks(beforeText));
      }

      // Add bold text
      parts.push(
        <strong key={`bold-${match.index}`} className="font-bold text-gray-900 dark:text-gray-100">
          {match[1]}
        </strong>
      );

      currentIndex = match.index + match[0].length;
    }

    // Add remaining text
    if (currentIndex < text.length) {
      const remainingText = text.slice(currentIndex);
      parts.push(...renderCodeAndBackticks(remainingText));
    }

    return <span>{parts}</span>;
  };

  // Handle `code` and backticks
  const renderCodeAndBackticks = (text: string): (string | React.ReactElement)[] => {
    const parts: (string | React.ReactElement)[] = [];
    let currentIndex = 0;

    // Handle `code` text
    const codeRegex = /`([^`]+)`/g;
    let match;

    while ((match = codeRegex.exec(text)) !== null) {
      // Add text before the match
      if (match.index > currentIndex) {
        parts.push(text.slice(currentIndex, match.index));
      }

      // Add code text
      parts.push(
        <code key={`code-${match.index}`} className="bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 px-1 py-0.5 rounded text-sm font-mono">
          {match[1]}
        </code>
      );

      currentIndex = match.index + match[0].length;
    }

    // Add remaining text
    if (currentIndex < text.length) {
      parts.push(text.slice(currentIndex));
    }

    return parts;
  };

  useEffect(() => {
    if (selectedBuild) {
      loadAIInsights();
    } else {
      setAiInsights(null);
    }
  }, [selectedBuild]);

  const loadAIInsights = async () => {
    if (!selectedBuild) return;

    try {
      setLoading(true);
      logger.debug('Loading AI insights for build', 'AI_INSIGHTS', {
        jobName: selectedBuild.originalJobName,
        buildId: selectedBuild.buildId
      }, { component: 'AIInsightsPanel', action: 'loadAIInsights' });

      const response = await dashboardApi.getAIInsights(selectedBuild.originalJobName, selectedBuild.buildId);
      logger.debug('AI insights API response received', 'AI_INSIGHTS', {
        responseType: typeof response,
        responseKeys: Object.keys(response || {}),
        hasData: response && typeof response === 'object'
      }, { component: 'AIInsightsPanel', action: 'loadAIInsights' });

      // Check if response is wrapped in hasData structure or direct data
      if (response && typeof response === 'object') {
        // Type guard for wrapped format
        if ('hasData' in response) {
          // Wrapped format: { hasData: boolean, data?: AIInsightsDTO }
          const wrappedResponse = response as { hasData: boolean; data?: AIInsightsDTO; message?: string };
          if (wrappedResponse.hasData && wrappedResponse.data) {
            logger.debug('Using wrapped format AI insights', 'AI_INSIGHTS', {
              hasData: wrappedResponse.hasData
            }, { component: 'AIInsightsPanel', action: 'loadAIInsights' });
            setAiInsights(wrappedResponse.data);
          } else {
            logger.debug('Wrapped format but no AI insights data available', 'AI_INSIGHTS', undefined, {
              component: 'AIInsightsPanel',
              action: 'loadAIInsights'
            });
            setAiInsights(null);
          }
        } else if ('criticalSecretExposure' in response || 'summary' in response || 'recommendations' in response) {
          // Direct format: AIInsightsDTO (supports both new and legacy formats)
          logger.debug('Using direct format AI insights', 'AI_INSIGHTS', {
            hasLegacyFields: 'criticalSecretExposure' in response,
            hasNewFields: 'summary' in response || 'recommendations' in response,
            responseKeys: Object.keys(response)
          }, {
            component: 'AIInsightsPanel',
            action: 'loadAIInsights'
          });
          setAiInsights(response as AIInsightsDTO);
        } else {
          logger.warn('Unknown AI insights response format', 'AI_INSIGHTS', { response }, {
            component: 'AIInsightsPanel',
            action: 'loadAIInsights'
          });
          setAiInsights(null);
        }
      } else {
        logger.warn('Invalid AI insights response', 'AI_INSIGHTS', { response }, {
          component: 'AIInsightsPanel',
          action: 'loadAIInsights'
        });
        setAiInsights(null);
      }
    } catch (error) {
      logger.error('Failed to load AI insights', 'AI_INSIGHTS', { error }, {
        component: 'AIInsightsPanel',
        action: 'loadAIInsights'
      });
      setAiInsights(null);
    } finally {
      setLoading(false);
    }
  };
  // Convert API insights to display format - supports both new and legacy formats
  const convertToDisplayInsights = (data: AIInsightsDTO): AIInsight[] => {
    const insights: AIInsight[] = [];

    // Handle new format (from actual API)
    if (data.summary) {
      insights.push({
        type: 'info',
        title: 'Build Summary',
        message: data.summary,
      });
    }

    if (data.recommendations && Array.isArray(data.recommendations) && data.recommendations.length > 0) {
      insights.push({
        type: 'success',
        title: 'AI Recommendations',
        message: data.recommendations.map((rec, index) => `${index + 1}. ${rec}`).join('\n\n'),
      });
    }

    if (data.trends) {
      const trendsMessage = [];
      if (data.trends.pattern) {
        trendsMessage.push(`**Pattern:** ${data.trends.pattern}`);
      }
      if (data.trends.direction) {
        trendsMessage.push(`**Direction:** ${data.trends.direction}`);
      }

      if (trendsMessage.length > 0) {
        insights.push({
          type: data.trends.direction === 'negative' ? 'warning' : 'info',
          title: 'Trend Analysis',
          message: trendsMessage.join('\n\n'),
        });
      }
    }

    // Handle legacy format (for backward compatibility)
    if (data.criticalSecretExposure) {
      insights.push({
        type: 'error',
        title: 'Critical Secret Exposure',
        message: data.criticalSecretExposure,
      });
    }

    if (data.dependencyManagement) {
      insights.push({
        type: 'warning',
        title: 'Dependency Management',
        message: data.dependencyManagement,
      });
    }

    if (data.securityTrendAlert) {
      insights.push({
        type: 'info',
        title: 'Security Trend Alert',
        message: data.securityTrendAlert,
      });
    }

    if (data.recommendation) {
      // ✅ FIX: Handle recommendation as string OR array
      const recommendationMessage = Array.isArray(data.recommendation)
        ? data.recommendation.map((rec, index) => `${index + 1}. ${rec}`).join('\n\n')
        : data.recommendation;

      insights.push({
        type: 'success',
        title: 'Recommendations',
        message: recommendationMessage,
      });
    }

    return insights.filter(insight => insight.message && insight.message.trim().length > 0);
  };

  // Handle different states
  if (!hasBuilds) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow mb-6">
        <div className="p-4 border-b border-gray-100 dark:border-gray-700">
          <div className="flex items-center">
            <i className="fas fa-brain text-blue-500 mr-2"></i>
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">AI Insights</h3>
          </div>
        </div>
        <div className="p-8 flex flex-col items-center justify-center text-center">
          <i className="fas fa-robot text-4xl text-gray-400 mb-4"></i>
          <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">No AI Insights Available</h4>
          <p className="text-sm text-gray-500 dark:text-gray-400 max-w-md">
            AI-powered security insights will appear here after you trigger builds in Jenkins.
          </p>
        </div>
      </div>
    );
  }

  if (!selectedBuild) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow mb-6">
        <div className="p-4 border-b border-gray-100 dark:border-gray-700">
          <div className="flex items-center">
            <i className="fas fa-brain text-blue-500 mr-2"></i>
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">AI Insights</h3>
          </div>
        </div>
        <div className="p-4 text-center text-gray-500 dark:text-gray-400">
          <p>Select a build to view AI-powered security insights</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow mb-6">
        <div className="p-4 border-b border-gray-100 dark:border-gray-700">
          <div className="flex items-center">
            <i className="fas fa-brain text-blue-500 mr-2"></i>
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">AI Insights</h3>
          </div>
        </div>
        <div className="p-4 flex items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mr-3"></div>
          <span className="text-gray-500 dark:text-gray-400">Analyzing build data...</span>
        </div>
      </div>
    );
  }

  const displayInsights = aiInsights ? convertToDisplayInsights(aiInsights) : [];

  const getInsightStyles = (type: string) => {
    switch (type) {
      case 'info':
        return 'bg-blue-50 dark:bg-blue-900/20 border-l-4 border-blue-500';
      case 'warning':
        return 'bg-yellow-50 dark:bg-yellow-900/20 border-l-4 border-yellow-500';
      case 'error':
        return 'bg-red-50 dark:bg-red-900/20 border-l-4 border-red-500';
      case 'success':
        return 'bg-green-50 dark:bg-green-900/20 border-l-4 border-green-500';
      default:
        return 'bg-gray-50 dark:bg-gray-900/20 border-l-4 border-gray-500';
    }
  };

  if (displayInsights.length === 0) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow mb-6">
        <div className="p-4 border-b border-gray-100 dark:border-gray-700">
          <div className="flex items-center">
            <i className="fas fa-brain text-blue-500 mr-2"></i>
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">AI Insights</h3>
            <span className="ml-2 text-xs text-gray-500 dark:text-gray-400">
              {selectedBuild?.originalJobName} #{selectedBuild?.buildId}
            </span>
          </div>
        </div>
        <div className="p-4 text-center text-gray-500 dark:text-gray-400">
          <i className="fas fa-exclamation-triangle text-2xl mb-2"></i>
          <p>No AI insights available for this build</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow mb-6">
      <div className="p-4 border-b border-gray-100 dark:border-gray-700">
        <div className="flex items-center">
          <i className="fas fa-brain text-blue-500 mr-2"></i>
          <h3 className="font-semibold text-gray-900 dark:text-gray-100">AI Insights</h3>
          <span className="ml-2 text-xs text-gray-500 dark:text-gray-400">
            {selectedBuild?.originalJobName} #{selectedBuild?.buildId}
          </span>
        </div>
      </div>
      <div className="p-4">
        {displayInsights.map((insight: AIInsight, index: number) => (
          <div key={index} className={`mb-4 p-4 rounded ${getInsightStyles(insight.type)}`}>
            <h4 className="font-medium text-gray-900 dark:text-gray-100 mb-3">{insight.title}</h4>
            <div className="text-sm text-gray-800 dark:text-gray-200">
              {renderMarkdown(insight.message)}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AIInsightsPanel;
