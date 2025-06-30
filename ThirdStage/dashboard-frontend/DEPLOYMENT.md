# DevSecOps AI Dashboard - Deployment Guide

## Project Overview

The **DevSecOps AI Dashboard** is a modern React-based web application that provides comprehensive monitoring and analysis of Jenkins CI/CD pipelines with AI-powered security insights. The dashboard offers real-time visibility into build statuses, security anomalies, and performance metrics to help development teams maintain secure and efficient DevOps workflows.

### Key Features

- **Real-time Dashboard**: Live monitoring of Jenkins jobs and builds with 30-second auto-refresh
- **Security Analytics**: AI-powered detection and analysis of security anomalies in builds
- **Build Management**: Detailed build information, logs, and rerun capabilities
- **Risk Assessment**: Risk scoring and trend analysis for security posture
- **Interactive Charts**: Visual representation of anomaly trends and severity distributions
- **Job Explorer**: Comprehensive view of all Jenkins jobs with status and anomaly counts
- **Dark/Light Theme**: User-friendly interface with theme switching
- **Responsive Design**: Mobile-friendly layout with Tailwind CSS

### Technology Stack

- **Frontend**: React 19.1.0 with TypeScript
- **Build Tool**: Vite 6.3.5
- **Styling**: Tailwind CSS 3.4.17 with dark mode support
- **Charts**: ApexCharts 4.7.0 and Chart.js 4.4.9
- **Icons**: FontAwesome 6.7.2, Heroicons 2.2.0, Lucide React
- **HTTP Client**: Axios 1.9.0
- **Development**: ESLint, PostCSS, Autoprefixer
- **Containerization**: Docker with multi-stage builds

## Architecture Summary

The application follows a modern React architecture with:

- **Component-based Structure**: Modular components for dashboard sections
- **Service Layer**: Centralized API service with error handling and validation
- **State Management**: React hooks for local state and data fetching
- **Utility Layer**: Logging, validation, error handling, and performance monitoring
- **Configuration**: Environment-based configuration with constants
- **Responsive Design**: Mobile-first approach with Tailwind CSS

### Key Components

- `Dashboard.tsx`: Main dashboard orchestrating all components
- `SummaryStats.tsx`: Key metrics display (total jobs, active builds, security anomalies)
- `RecentJobBuilds.tsx`: Recent build history and status
- `BuildDetails.tsx`: Detailed build information and logs
- `AnomaliesTable.tsx`: Security anomaly detection results
- `AIInsightsPanel.tsx`: AI-powered security recommendations
- `JobExplorer.tsx`: Comprehensive job management interface
- Chart components for data visualization

## Prerequisites

### System Requirements

- **Node.js**: Version 20 or higher
- **npm**: Version 9 or higher (comes with Node.js)
- **Docker**: Version 20.10+ (for containerized deployment)
- **Docker Compose**: Version 2.0+ (for orchestrated deployment)

### Backend API Requirements

The dashboard requires a backend API server running on:
- **Code Default URL**: `http://localhost:8282/api/dashboard`
- **Docker Default URL**: `http://localhost:8080/api`
- **Current Configuration**: `http://158.160.128.12:8383/api/dashboard`
- **Configurable via**: `VITE_API_BASE_URL` environment variable

## Manual Deployment (Without Docker)

### 1. Clone and Setup

```bash
# Navigate to the dashboard frontend directory
cd dashboard-frontend

# Install dependencies
npm install
```

### 2. Environment Configuration

Create a `.env` file in the `dashboard-frontend` directory:

```env
# API Configuration
VITE_API_BASE_URL=http://158.160.128.12:8383/api/dashboard

# Application Configuration (Note: These are defined but not currently used in code)
VITE_APP_TITLE=DevSecOps AI Dashboard
VITE_APP_VERSION=1.0.0

# Feature Flags (Note: These are defined but not currently used in code)
VITE_ENABLE_DARK_MODE=true
VITE_ENABLE_AUTO_REFRESH=true
VITE_REFRESH_INTERVAL=30000

# Development Configuration (optional)
NODE_ENV=production
```

### 3. Build and Run

#### Development Mode
```bash
# Start development server with hot reload
npm run dev

# Application will be available at http://localhost:3000
```

#### Production Build
```bash
# Build for production
npm run build

# Preview production build
npm run preview

# Or serve with a static file server
npx serve -s dist -l 3000
```

### 4. Verification

1. Open browser to `http://localhost:3000`
2. Verify dashboard loads without errors
3. Check browser console for any API connection issues
4. Confirm all dashboard sections display properly

## Docker Compose Deployment (Recommended)

### 1. Quick Start

```bash
# Navigate to dashboard frontend directory
cd dashboard-frontend

# Start the application
make up

# Or use docker compose directly
docker compose up -d
```

### 2. Environment Configuration

Create a `.env` file for Docker Compose:

```env
# Port Configuration
FRONTEND_PORT=3000
FRONTEND_HMR_PORT=24678

# API Configuration (Docker default is http://localhost:8080/api)
VITE_API_BASE_URL=http://158.160.128.12:8383/api/dashboard

# Application Configuration (Docker default title is "Jenkins Security Dashboard")
VITE_APP_TITLE=DevSecOps AI Dashboard
VITE_APP_VERSION=1.0.0

# Feature Flags (Note: These are defined but not currently used in code)
VITE_ENABLE_DARK_MODE=true
VITE_ENABLE_AUTO_REFRESH=true
VITE_REFRESH_INTERVAL=30000

# Docker Compose Environment Variables (these ARE used by docker-compose)
VITE_ENABLE_MOCK_DATA=true
VITE_LOG_LEVEL=debug
VITE_PERFORMANCE_MONITORING=true

# Environment
NODE_ENV=development
```

### 3. Available Make Commands

```bash
# Start services
make up

# Stop services
make down

# View logs
make logs

# Rebuild containers
make rebuild

# Access container shell
make shell

# Clean up (removes volumes)
make clean

# Restart services
make restart
```

### 4. Docker Configuration Details

The Docker setup includes:
- **Development optimized**: Hot reload and file watching enabled
- **Volume mounts**: Source code mounted for live development
- **Health checks**: Automatic service health monitoring
- **Network isolation**: Dedicated bridge network
- **Port mapping**: Configurable ports for frontend and HMR

## Configuration Options

### Environment Variables

#### **Functional Environment Variables (Actually Used in Code)**
| Variable | Code Default | Docker Default | Description |
|----------|-------------|----------------|-------------|
| `VITE_API_BASE_URL` | `http://localhost:8282/api/dashboard` | `http://localhost:8080/api` | Backend API base URL |
| `NODE_ENV` | `development` | `development` | Environment mode (affects logging behavior) |
| `FRONTEND_PORT` | `3000` | `3000` | Frontend application port (Docker only) |
| `FRONTEND_HMR_PORT` | `24678` | `24678` | Hot module replacement port (Docker only) |

#### **Defined But Non-Functional Environment Variables**
These variables are defined in .env files but are **NOT currently implemented** in the application code:

| Variable | Current Value | Description |
|----------|---------------|-------------|
| `VITE_APP_TITLE` | `DevSecOps AI Dashboard` | Application title (hard-coded as "DevSecOps AI") |
| `VITE_APP_VERSION` | `1.0.0` | Application version (not displayed) |
| `VITE_ENABLE_DARK_MODE` | `true` | Dark mode toggle (always available) |
| `VITE_ENABLE_AUTO_REFRESH` | `true` | Auto-refresh feature (hard-coded to 30s) |
| `VITE_REFRESH_INTERVAL` | `30000` | Refresh interval (hard-coded to 30000ms) |

#### **Docker Compose Only Variables**
These variables are used by docker-compose.yml but may not affect the application:

| Variable | Docker Default | Description |
|----------|----------------|-------------|
| `VITE_ENABLE_MOCK_DATA` | `true` | Enable mock data for development |
| `VITE_LOG_LEVEL` | `debug` | Logging level (debug, info, warn, error) |
| `VITE_PERFORMANCE_MONITORING` | `true` | Enable performance monitoring |

### Application Features

- **Auto-refresh**: Dashboard updates every 30 seconds (hard-coded, not configurable via environment)
- **Time ranges**: 7 days, 14 days, 30 days, 60 days, 180 days, all time
- **Job filtering**: Filter by specific Jenkins jobs or view all
- **Theme switching**: Dark/light mode toggle (always available, not environment-controlled)
- **Responsive design**: Mobile and desktop optimized
- **Application Title**: Hard-coded as "DevSecOps AI" (not controlled by VITE_APP_TITLE)

### Hard-coded Configuration

The following features are **hard-coded** in the application and cannot be changed via environment variables:

- **Auto-refresh interval**: 30 seconds (defined in `src/constants/dashboard.ts`)
- **Application title**: "DevSecOps AI" (defined in Dashboard component)
- **API timeout**: 10 seconds (defined in `src/services/api.ts`)
- **Theme availability**: Always available (not controlled by VITE_ENABLE_DARK_MODE)
- **Performance monitoring**: Built-in but not environment-controlled

## Verification Steps

### 1. Application Health Check

```bash
# Check if application is running
curl -f http://localhost:3000

# Check API connectivity (if backend is running)
curl -f http://158.160.128.12:8383/api/dashboard/recentJobBuilds
```

### 2. Browser Verification

1. **Dashboard Loading**: Verify main dashboard loads without errors
2. **API Connection**: Check browser console for API connection status
3. **Components**: Ensure all dashboard sections render properly:
   - Summary statistics
   - Recent job builds
   - Build details
   - Security anomalies
   - Charts and trends
   - AI insights
   - Job explorer

### 3. Functionality Testing

1. **Theme Toggle**: Test dark/light mode switching
2. **Responsive Design**: Test on different screen sizes
3. **Auto-refresh**: Verify 30-second auto-refresh functionality
4. **Filtering**: Test job and time range filtering
5. **Build Selection**: Test build selection and detail viewing

## Troubleshooting

### Common Issues

1. **Port Conflicts**: Change `FRONTEND_PORT` if port 3000 is in use
2. **API Connection**: Verify backend API is running and accessible at the configured URL
   - Current API URL: `http://158.160.128.12:8383/api/dashboard`
   - Docker default: `http://localhost:8080/api`
   - Code default: `http://localhost:8282/api/dashboard`
3. **Build Errors**: Clear node_modules and reinstall dependencies
4. **Docker Issues**: Check Docker daemon is running and ports are available
5. **Environment Variables**: Remember that many VITE_ variables are defined but not functional
6. **Configuration Mismatch**: Docker compose defaults may differ from .env file values

### Logs and Debugging

```bash
# View application logs (Docker)
make logs

# View detailed logs
docker compose logs -f dashboard-frontend

# Access container for debugging
make shell

# Check application health
curl -f http://localhost:3000/health
```

### Performance Optimization

- Enable performance monitoring in development
- Use production build for deployment
- Configure appropriate log levels
- Monitor memory usage and API response times

## Important Notes

### Configuration Reality Check

This application has **limited environment variable support**. Most configuration is hard-coded:

1. **Only VITE_API_BASE_URL is functional** - all other VITE_ variables are ignored by the code
2. **App title is hard-coded** as "DevSecOps AI" regardless of VITE_APP_TITLE
3. **Auto-refresh is hard-coded** to 30 seconds regardless of VITE_REFRESH_INTERVAL
4. **Theme switching is always available** regardless of VITE_ENABLE_DARK_MODE
5. **Docker compose uses different defaults** than the code defaults

### Current API Configuration

- **Production API**: `http://158.160.128.12:8383/api/dashboard`
- **Docker Default**: `http://localhost:8080/api`
- **Code Default**: `http://localhost:8282/api/dashboard`

Make sure your backend API is running on the URL specified in your VITE_API_BASE_URL.

## Next Steps

1. **Backend Integration**: Ensure Jenkins backend API is properly configured at the correct URL
2. **Environment Variable Implementation**: Consider implementing the defined but non-functional environment variables
3. **Configuration Consistency**: Align docker-compose defaults with actual usage
4. **Security Configuration**: Set up proper authentication and authorization
5. **Monitoring**: Implement application monitoring and alerting
6. **Scaling**: Consider load balancing for high-traffic environments

For additional support or customization, refer to the component documentation in the `src/components` directory.
