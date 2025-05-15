# Stage 1: Build the React application
# Use an official Node.js Alpine image for a smaller size
FROM node:22-alpine AS builder
WORKDIR /app

# Copy package.json and package-lock.json (or yarn.lock if you use Yarn)
COPY package*.json ./

# Install dependencies
# Using --frozen-lockfile or ci is often recommended for reproducible builds
RUN npm ci

# Copy the rest of the application code
COPY . .

# Build the application for production
# Vite will output to the 'dist' folder by default
RUN npm run build

# Stage 2: Serve the static files with Nginx
# Use an official Nginx Alpine image for a small footprint
FROM nginx:1.27-alpine
# (You can check Docker Hub for the latest stable Nginx Alpine version, e.g., 1.27-alpine as of May 2024)

# Copy the built static files from the 'builder' stage's 'dist' folder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy our custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80 (Nginx default HTTP port)
EXPOSE 80

# Command to start Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]