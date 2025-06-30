// DOM debugging utility to check what elements are actually present
import { logger } from './logger';

export const debugDOMElements = (): void => {
  logger.info('🔍 Starting DOM element debugging', 'DOM_DEBUGGER');
  
  // Check all the elements we're looking for
  const elementsToCheck = [
    '.main-content',
    'header[role="banner"]',
    'aside[role="navigation"]',
    'main[role="main"]',
    '[data-component="Dashboard"]',
    '[data-component="DashboardMain"]'
  ];
  
  console.log('🔍 DOM Element Debug Report:');
  console.log('============================');
  
  elementsToCheck.forEach(selector => {
    const element = document.querySelector(selector);
    if (element) {
      console.log(`✅ FOUND: ${selector}`);
      console.log(`   - Tag: ${element.tagName}`);
      console.log(`   - Classes: ${element.className}`);
      console.log(`   - ID: ${element.id || 'none'}`);
      console.log(`   - Data attributes:`, Array.from(element.attributes).filter(attr => attr.name.startsWith('data-')));
    } else {
      console.log(`❌ MISSING: ${selector}`);
    }
    console.log('---');
  });
  
  // Also check what elements are actually in the DOM
  console.log('🔍 All elements with role attributes:');
  const elementsWithRoles = document.querySelectorAll('[role]');
  elementsWithRoles.forEach(el => {
    console.log(`- ${el.tagName.toLowerCase()}[role="${el.getAttribute('role')}"] - classes: ${el.className}`);
  });
  
  console.log('🔍 All elements with data-component attributes:');
  const elementsWithDataComponent = document.querySelectorAll('[data-component]');
  elementsWithDataComponent.forEach(el => {
    console.log(`- ${el.tagName.toLowerCase()}[data-component="${el.getAttribute('data-component')}"] - classes: ${el.className}`);
  });
  
  console.log('🔍 All main-content elements:');
  const mainContentElements = document.querySelectorAll('.main-content');
  mainContentElements.forEach(el => {
    console.log(`- ${el.tagName.toLowerCase()}.main-content - role: ${el.getAttribute('role')} - data-component: ${el.getAttribute('data-component')}`);
  });
  
  console.log('============================');
  logger.info('🔍 DOM element debugging completed', 'DOM_DEBUGGER');
};

// Make available globally in development
if (process.env.NODE_ENV === 'development') {
  (window as any).debugDOM = debugDOMElements;
}

export default {
  debugDOMElements
};
