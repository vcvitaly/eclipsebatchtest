ALTER SEQUENCE project_asset_id_seq INCREMENT BY 300;
SELECT setval('project_asset_id_seq', COALESCE((SELECT MAX(id) FROM project_asset), 0) + 300);

alter sequence project_asset_id_seq as bigint;
alter table project_asset alter id type bigint;