-- Execute manualmente, com a API parada e após backup.
-- Remove somente repetições da MESMA associação entre grupo e usuário.
BEGIN;
LOCK TABLE grupo_membros IN ACCESS EXCLUSIVE MODE;
DELETE FROM grupo_membros a USING grupo_membros b
WHERE a.grupo_id = b.grupo_id AND a.usuario_id = b.usuario_id AND a.ctid > b.ctid;
CREATE UNIQUE INDEX IF NOT EXISTS uq_grupo_membro ON grupo_membros (grupo_id, usuario_id);
COMMIT;
