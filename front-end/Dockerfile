
FROM node:20-alpine AS builder

WORKDIR /app


COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build

FROM node:20-alpine

WORKDIR /app

COPY package*.json ./

RUN npm ci --omit=dev

COPY --from=builder /app/.nuxt ./.nuxt
COPY --from=builder /app/nuxt.config.ts ./nuxt.config.ts
COPY --from=builder /app/.output ./.output

EXPOSE 3001

ENV HOST=0.0.0.0
ENV PORT=3001
ENV NODE_ENV=production

CMD ["node", ".output/server/index.mjs"]

