#!/bin/bash
# Aguarda o SQL Server iniciar e executa os scripts de inicialização.
# Usado apenas se você precisar rodar init via entrypoint customizado.

set -e

echo "Aguardando SQL Server ficar disponivel..."
until /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "SELECT 1" &>/dev/null; do
    sleep 2
done

echo "SQL Server disponivel. Executando scripts de inicializacao..."
for f in /docker-entrypoint-initdb.d/*.sql; do
    echo "Executando: $f"
    /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -i "$f"
done

echo "Inicializacao concluida."
