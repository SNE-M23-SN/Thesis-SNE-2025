FROM node:20-alpine AS development

WORKDIR /app

# Install system dependencies
RUN apk add --no-cache curl dumb-init

# Copy package files for dependency caching
COPY package.json package-lock.json* ./

# Install dependencies
RUN npm install && \
    chmod -R +x node_modules/.bin/ && \
    npm cache clean --force

# Copy source code
COPY . .

EXPOSE 3000 24678

# Use dumb-init for proper signal handling
ENTRYPOINT ["dumb-init", "--"]
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "3000"]

