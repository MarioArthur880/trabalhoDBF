-- Apenas diagnóstico: não altera dados. Execute no PostgreSQL do Liftly.
SELECT grupo_id, usuario_id, count(*) AS quantidade
FROM grupo_membros GROUP BY grupo_id, usuario_id HAVING count(*) > 1;
SELECT lower(trim(email)) AS email_normalizado, count(*) AS quantidade
FROM usuario GROUP BY lower(trim(email)) HAVING count(*) > 1;
-- Candidatos a histórico duplicado. Revise: duas sessões legítimas não devem ser apagadas.
SELECT usuario_id, divisao_id, data_inicio, count(*) AS quantidade
FROM historico GROUP BY usuario_id, divisao_id, data_inicio HAVING count(*) > 1;
