DROP FUNCTION IF EXISTS gls_reverse_geocoding(TEXT) CASCADE;
DROP TYPE IF EXISTS reverse_type CASCADE;

/* Create an especific type to return */
CREATE TYPE reverse_type AS (
	street text,
	munsub text,
	mun text,
	subcountry text,
	country text,
	distance DOUBLE PRECISION
);

/* Function to find an address */
CREATE FUNCTION gls_reverse_geocoding(geometry TEXT)
RETURNS SETOF reverse_type AS
$BODY$
DECLARE
	rec RECORD;
	geom GEOMETRY;
	res reverse_type;
BEGIN
	/* Sacamos la geometria a partir del parametro de entrada */
	geom := ST_GeomFromText(geometry, 4326);
	/* Seleccionamos la calle */
	FOR rec IN SELECT DISTINCT
		s.name as name_s, 
		ms.name as name_ms,
		m.name as name_m,
		cs.name as name_cs,
		c.name as name_c,
		(st_distance(geom, s.geometry) + 
		st_distance(geom, ms.geometry) +
		st_distance(geom, m.geometry) +
		st_distance(geom, cs.geometry) +
		st_distance(geom, c.geometry)) as result
	FROM 
		street as s,
		municipality_subdivision as ms,
		municipality as m,
		country_subdivision as cs,
		country as c
	where
		ST_DWithin(geom, s.geometry, 0.001) = true AND
		ST_DWithin(geom, ms.geometry, 0.001) = true AND
		ST_DWithin(geom, m.geometry, 0.001) = true AND
		ST_DWithin(geom, cs.geometry, 0.001) = true AND
		ST_DWithin(geom, c.geometry, 0.001) = true
	order by result asc LOOP
		/* Actualizamos el resultado con el nombre de la direccion */
		res.street := rec.name_s;
		RAISE INFO '%, %, %, %', res.munsub, res.mun, res.subcountry, res.country;
		res.munsub := rec.name_ms;
		res.mun := rec.name_m;
		res.subcountry := rec.name_cs;
		res.country := rec.name_c;
		res.distance := rec.result;
		RETURN NEXT res;
	END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql';