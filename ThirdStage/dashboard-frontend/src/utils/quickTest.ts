// Quick test to verify the component rendering fix
import { logger } from './logger';

export const runQuickComponentTest = async (): Promise<{
  success: boolean;
  foundElements: string[];
  missingElements: string[];
  details: string;
}> => {
  logger.info('🧪 Running quick component rendering test', 'QUICK_TEST');
  
  // Wait a bit for components to render
  await new Promise(resolve => setTimeout(resolve, 1000));
  
  const criticalElements = [
    '.main-content',
    'header[role="banner"]',
    'aside[role="complementary"]',
    'main[role="main"]',
    '[data-component="Dashboard"]',
    '[data-component="DashboardMain"]'
  ];
  
  const foundElements: string[] = [];
  const missingElements: string[] = [];
  
  for (const selector of criticalElements) {
    const element = document.querySelector(selector);
    if (element) {
      foundElements.push(selector);
      logger.debug(`✅ Found element: ${selector}`, 'QUICK_TEST');
    } else {
      missingElements.push(selector);
      logger.warn(`❌ Missing element: ${selector}`, 'QUICK_TEST');
    }
  }
  
  const success = missingElements.length === 0;
  const details = `Found ${foundElements.length}/${criticalElements.length} critical elements`;
  
  logger.info('🏁 Quick component test completed', 'QUICK_TEST', {
    success,
    foundCount: foundElements.length,
    missingCount: missingElements.length,
    totalElements: criticalElements.length
  });
  
  return {
    success,
    foundElements,
    missingElements,
    details
  };
};

// Make available globally in development
if (process.env.NODE_ENV === 'development') {
  (window as any).quickTest = {
    runQuickComponentTest
  };
}

export default {
  runQuickComponentTest
};
