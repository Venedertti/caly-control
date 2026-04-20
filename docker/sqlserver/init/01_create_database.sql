-- Criação do banco de desenvolvimento
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'otica_dev')
BEGIN
    CREATE DATABASE otica_dev;
    PRINT 'Banco otica_dev criado.';
END
GO
