FROM node:19.7-alpine3.17
WORKDIR /app
COPY package*.json ./
EXPOSE 8080

RUN npm install # --only=production
# Если вы создаете сборку для продакшн
# RUN npm ci --omit=dev

COPY . .
CMD ["npm", "run", "serve"]
#CMD ["npm", "run", "build"]
#CMD ["npm", "run", "lint"]



