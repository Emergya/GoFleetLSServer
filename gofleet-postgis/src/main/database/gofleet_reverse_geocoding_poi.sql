DROP FUNCTION IF EXISTS gofleet_reverse_geocoding_poi(TEXT, DOUBLE PRECISION, INTEGER) CASCADE;
DROP TYPE IF EXISTS reverse_type_poi;

/* Create an specific type to return */
CREATE TYPE reverse_type_poi AS (
	id_poi INTEGER,
	poi_distance DOUBLE PRECISION
);

CREATE FUNCTION gofleet_reverse_geocoding_poi(geometry TEXT, max_distance DOUBLE PRECISION, max_res INTEGER)
RETURNS SETOF reverse_type_poi AS
$BODY$
DECLARE
	query TEXT;
	rec RECORD;
	res reverse_type_poi;
BEGIN
	/* Common part of a query statement */
	query := 'SELECT id as poiid, st_distance(';
	/* Escape sentence to geometry parameter */
	query := query || E'\'';
	query := query ||geometry;
	query :=  query || E'\'';
	query := query || ', poi.geometry) as distance FROM poi ';
	/* Check if we have a maxim distance */
	IF(max_distance IS NOT NULL) THEN
		query := query || ' WHERE st_distance(';
		/* Escape sentence to geometry parameter */
		query := query || E'\'';
		query := query ||geometry;
		query := query || E'\'';
		query := query ||', poi.geometry) <= '|| max_distance;
	END IF;
	/* Check if we have a maxim results number */
	IF(max_res IS NOT NULL) THEN
		query := query || ' order by st_distance(';
		/* Escape sentence to geometry parameter */
		query := query || E'\'';
		query := query ||geometry;
		query := query || E'\'';
		query := query ||', poi.geometry) asc limit '|| max_res;
	END IF;
	/* Set the reverse_type_poi with the query results */
	FOR rec IN EXECUTE query LOOP
		res.id_poi := rec.poiid;
		res.poi_distance := rec.distance;
		RETURN NEXT res;
	END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';
