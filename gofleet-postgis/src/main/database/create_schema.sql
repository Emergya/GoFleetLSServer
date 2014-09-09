--WARNING: This script DROPS the ENTIRE DATABASE

-- INIT
\set database_name gofleetls
\set user_name gofleetls

;

\echo 'Droping old database and creating new one. If you do not want to drop your database, comment the following lines.'
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

DROP DATABASE IF EXISTS :database_name;
DROP USER IF EXISTS :user_name;
CREATE USER :user_name WITH PASSWORD 'gofleetls';
CREATE DATABASE :database_name WITH TEMPLATE = template_postgis ENCODING = 'UTF8' OWNER = :user_name;

\echo 'New database created: ' :database_name
\echo 'Giving grants to user: ' :user_name
\connect :database_name 

GRANT SELECT ON geometry_columns TO :user_name;
GRANT INSERT ON geometry_columns TO :user_name;
GRANT UPDATE ON geometry_columns TO :user_name;
GRANT DELETE ON geometry_columns TO :user_name;
GRANT SELECT ON spatial_ref_sys TO :user_name;

\echo 'Connecting to ' :database_name
\connect :database_name :user_name

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

-- TABLES
\echo 'Dropping and re-generating tables with dummy data'
DROP TABLE IF EXISTS configuration;
CREATE TABLE configuration (
    id character varying(20) NOT NULL,
    valor character varying,
    updatable boolean DEFAULT false NOT NULL
);
COMMENT ON TABLE configuration IS 'Configuration Table';
ALTER TABLE configuration ADD PRIMARY KEY (id);

--Routing tables

DROP TABLE IF EXISTS routing_template;
CREATE TABLE routing_template (
    id serial,
    gid bigint DEFAULT (-1),
    cost double precision DEFAULT (-1) NOT NULL,
    reverse_cost double precision DEFAULT (-1) NOT NULL,
	source integer NOT NULL,
	target integer NOT NULL,
	x1 double precision NOT NULL,
	y1 double precision NOT NULL,
	x2 double precision NOT NULL,
	y2 double precision NOT NULL,
    to_cost double precision DEFAULT (0) NOT NULL,
    rule text
);
SELECT AddGeometryColumn ('routing_template','geometry',4326, 'LINESTRING',2);
ALTER TABLE routing_template ADD PRIMARY KEY (gid);

-- Tables

DROP TABLE IF EXISTS country_template;
CREATE TABLE country_template (
    id serial,
    name character varying(3) NOT NULL
);
SELECT AddGeometryColumn ('country_template','geometry',4326, 'MULTIPOLYGON',2);
ALTER TABLE country_template ADD PRIMARY KEY (id);

DROP TABLE IF EXISTS country_subdivision_template;
CREATE TABLE country_subdivision_template (
    id serial,
    name character varying NOT NULL
);
SELECT AddGeometryColumn ('country_subdivision_template','geometry',4326, 'MULTIPOLYGON',2);
ALTER TABLE country_subdivision_template ADD PRIMARY KEY (id);

DROP TABLE IF EXISTS municipality_template;
CREATE TABLE municipality_template (
    id serial,
    name character varying NOT NULL
);
SELECT AddGeometryColumn ('municipality_template','geometry',4326, 'MULTIPOLYGON',2);
ALTER TABLE municipality_template ADD PRIMARY KEY (id);

DROP TABLE IF EXISTS municipality_subdivision_template;
CREATE TABLE municipality_subdivision_template (
    id serial,
    name character varying NOT NULL
);
SELECT AddGeometryColumn ('municipality_subdivision_template','geometry',4326, 'MULTIPOLYGON',2);
ALTER TABLE municipality_subdivision_template ADD PRIMARY KEY (id);

DROP TABLE IF EXISTS street_template;
CREATE TABLE street_template (
    id serial,
    name character varying NOT NULL
);
SELECT AddGeometryColumn ('street_template','geometry',4326, 'LINESTRING',2);
ALTER TABLE street_template ADD PRIMARY KEY (id);

DROP TABLE IF EXISTS poi_template;
CREATE TABLE poi_template (
    id serial,
    name character varying
);
SELECT AddGeometryColumn ('poi_template','geometry',4326, 'POINT',2);
ALTER TABLE poi_template ADD PRIMARY KEY (id);

DROP TABLE IF EXISTS postal_code_template;
CREATE TABLE postal_code_template (
    id serial,
    code character varying NOT NULL,
    type character varying
);
SELECT AddGeometryColumn ('postal_code_template','geometry',4326, 'MULTIPOLYGON',2);
ALTER TABLE postal_code_template ADD PRIMARY KEY (id);

DROP TABLE IF EXISTS advanced_poi_template;
CREATE TABLE advanced_poi_template (
    id serial,
    key character varying NOT NULL,
    value character varying NOT NULL,
    poi bigint NOT NULL
);
ALTER TABLE advanced_poi_template ADD PRIMARY KEY (id);

\echo 'GofleetLS database created'

\echo 'Initializing dummy data'

CREATE OR REPLACE FUNCTION gls_updateTable (suffix VARCHAR, view_ VARCHAR) RETURNS void AS $$
	DECLARE
		SQL VARCHAR;
    BEGIN
	    RAISE DEBUG 'gls_updateTable(%, %)', suffix, view_;
	    SQL := 'CREATE OR REPLACE VIEW ' || view_ || ' AS SELECT * FROM ' || view_ || '_' || suffix || ';'; 
	    RAISE DEBUG '%',SQL;
	    EXECUTE SQL;
    END;
  $$ LANGUAGE plpgsql VOLATILE;
  
CREATE OR REPLACE FUNCTION gls_createTable (suffix VARCHAR, table_ VARCHAR) RETURNS void AS $$
	DECLARE
		SQL VARCHAR;
    BEGIN
	    SQL := 'CREATE TABLE ' || table_ || '_' || suffix ||' (LIKE ' || table_ || '_template including constraints);';
	    EXECUTE SQL;
	    RAISE DEBUG '%', SQL;
	    SQL := 'ALTER TABLE ' || table_ || '_' || suffix ||' ADD CONSTRAINT ' || table_ || '_' || suffix ||'_id PRIMARY KEY (id);';
	    EXECUTE SQL;
    END;
  $$ LANGUAGE plpgsql VOLATILE;
  
 CREATE OR REPLACE FUNCTION gls_updateAllTables (suffix VARCHAR) RETURNS void AS $$
    BEGIN
	    PERFORM gls_updateTable(suffix, 'routing');
	    PERFORM gls_updateTable(suffix, 'country');
	    PERFORM gls_updateTable(suffix, 'country_subdivision');
	    PERFORM gls_updateTable(suffix, 'municipality');
	    PERFORM gls_updateTable(suffix, 'municipality_subdivision');
	    PERFORM gls_updateTable(suffix, 'street');
	    PERFORM gls_updateTable(suffix, 'poi');
	    PERFORM gls_updateTable(suffix, 'postal_code');
	    PERFORM gls_updateTable(suffix, 'advanced_poi');
    END;
  $$ LANGUAGE plpgsql VOLATILE;
  
  
 CREATE OR REPLACE FUNCTION gls_createAllTables (suffix VARCHAR) RETURNS void AS $$
    BEGIN
	    PERFORM gls_createTable(suffix, 'routing');
	    PERFORM gls_createTable(suffix, 'country');
	    PERFORM gls_createTable(suffix, 'country_subdivision');
	    PERFORM gls_createTable(suffix, 'municipality');
	    PERFORM gls_createTable(suffix, 'municipality_subdivision');
	    PERFORM gls_createTable(suffix, 'street');
	    PERFORM gls_createTable(suffix, 'poi');
	    PERFORM gls_createTable(suffix, 'postal_code');
	    PERFORM gls_createTable(suffix, 'advanced_poi');
    END;
  $$ LANGUAGE plpgsql VOLATILE;
  
  
CREATE OR REPLACE FUNCTION gls_generateDummy () RETURNS void AS $$
	DECLARE 
		suffix NUMERIC;
    BEGIN
	    SELECT round(extract(epoch from now())*1000) INTO suffix;
	    RAISE INFO 'Creating tables with suffix: %',suffix;
	    PERFORM gls_createAllTables(suffix::VARCHAR);
	    PERFORM gls_updateAllTables(suffix::VARCHAR);
	    
	    RAISE INFO 'Generating Dummy data';
	    EXECUTE 'INSERT INTO country_' || suffix || ' (id, name, geometry) ' ||
	    		'VALUES (1, \'ES\', ' || 
	    		'ST_SetSRID(GeomFromText(\'MULTIPOLYGON(((44.044 -10.09,43.02 4.8,36.08 2, 35.5 10, 44.044 -10.09)))\'), 4326));';
	    EXECUTE 'INSERT INTO country_subdivision_' || suffix || ' (id, name, geometry) ' ||
	    		'VALUES (1, \'Imaginary Subdivision\', ' || 
	    		'ST_SetSRID(GeomFromText(\'MULTIPOLYGON(((44.044 -10.09,43.02 4.8,36.08 2, 35.5 10, 44.044 -10.09)))\'), 4326));';
	    EXECUTE 'INSERT INTO municipality_' || suffix || ' (id, name, geometry) ' ||
	    		'VALUES (1, \'Imaginary Municipality\', ' || 
	    		'ST_SetSRID(GeomFromText(\'MULTIPOLYGON(((44.044 -10.09,43.02 4.8,36.08 2, 35.5 10, 44.044 -10.09)))\'), 4326));';
	    EXECUTE 'INSERT INTO municipality_subdivision_' || suffix || ' (id, name, geometry) ' ||
	    		'VALUES (1, \'Imaginary Municipality Subdivision\', ' || 
	    		'ST_SetSRID(GeomFromText(\'MULTIPOLYGON(((44.044 -10.09,43.02 4.8,36.08 2, 35.5 10, 44.044 -10.09)))\'), 4326));';
	    EXECUTE 'INSERT INTO street_' || suffix || ' (id, name, geometry) ' ||
	    		'VALUES (1, \'Imaginary Municipality Subdivision\', ' || 
	    		'ST_SetSRID(GeomFromText(\'LINESTRING(40 -3,38 -1,36.08 2)\'), 4326));';
	    		
	    --Routing example
	    
	    EXECUTE 'INSERT INTO routing_' || suffix || ' (gid, id, geometry, cost, reverse_cost, source, target, x1, y1, x2, y2, to_cost, rule) ' ||
	    		'VALUES (-1, 10, ST_SetSRID(GeomFromText(\'LINESTRING(0 1,1 1)\'), 4326), ' ||
	    		' 10, \'infinity\', 1, 2, 0, 1, 1, 1, 10, \'\');';
	    EXECUTE 'INSERT INTO routing_' || suffix || ' (gid, id, geometry, cost, reverse_cost, source, target, x1, y1, x2, y2, to_cost, rule) ' ||
	    		'VALUES (-1, 20, ST_SetSRID(GeomFromText(\'LINESTRING(1 1,1 0)\'), 4326), ' ||
	    		' 20, \'infinity\', 2, 3, 1, 1, 1, 0, 10, \'\');';
	    EXECUTE 'INSERT INTO routing_' || suffix || ' (gid, id, geometry, cost, reverse_cost, source, target, x1, y1, x2, y2, to_cost, rule) ' ||
	    		'VALUES (-1, 30, ST_SetSRID(GeomFromText(\'LINESTRING(1 0,0 0)\'), 4326), ' ||
	    		' 30, \'infinity\', 3, 4, 1, 0, 0, 0, 10, \'\');';
	    EXECUTE 'INSERT INTO routing_' || suffix || ' (gid, id, geometry, cost, reverse_cost, source, target, x1, y1, x2, y2, to_cost, rule) ' ||
	    		'VALUES (-1, 40, ST_SetSRID(GeomFromText(\'LINESTRING(1 0,0 1)\'), 4326), ' ||
	    		' 40, \'infinity\', 3, 1, 1, 0, 0, 1, 10, \'\');';
	    EXECUTE 'INSERT INTO routing_' || suffix || ' (gid, id, geometry, cost, reverse_cost, source, target, x1, y1, x2, y2, to_cost, rule) ' ||
	    		'VALUES (-1, 50, ST_SetSRID(GeomFromText(\'LINESTRING(1 0,2 0)\'), 4326), ' ||
	    		' 50, 50, 1, 5, 1, 0, 2, 0, 10, \'\');';
	    EXECUTE 'INSERT INTO routing_' || suffix || ' (gid, id, geometry, cost, reverse_cost, source, target, x1, y1, x2, y2, to_cost, rule) ' ||
	    		'VALUES (-1, 60, ST_SetSRID(GeomFromText(\'LINESTRING(1 0,2 0)\'), 4326), ' ||
	    		' 60, 60, 4, 5, 0, 0, 2, 0, 10, \'\');';
	    
    END;
  $$ LANGUAGE plpgsql VOLATILE;
  
  SELECT gls_generateDummy();
