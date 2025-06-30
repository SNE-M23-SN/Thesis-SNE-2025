.PHONY: up down build logs shell clean

up:
	docker compose up -d

down:
	docker compose down

build:
	docker compose build --no-cache

logs:
	docker compose logs -f dashboard-frontend

shell:
	docker compose exec dashboard-frontend sh

clean:
	docker compose down -v
	docker system prune -f

restart: down up

rebuild: down build up
