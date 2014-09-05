DROP TYPE IF EXISTS geocoding_address_type CASCADE;

/* Create a specific type to identify an address */
CREATE TYPE geocoding_address_type AS(
	street text,
	munsub text,
	mun text,
	subcountry text,
	country text
);
/* Reomove function and types */
DROP FUNCTION IF EXISTS gofleet_geocoding(geocoding_address_type) CASCADE;
DROP TYPE IF EXISTS geocoding_type CASCADE;

/* Create a specific type to return the result */
CREATE TYPE geocoding_type AS(
	address geocoding_address_type,
	geometry GEOMETRY
);


/* Create a function to return the geometry associated to an address */
CREATE FUNCTION gls_geocoding(street character varying, munsub character varying, mun character varying, subcountry character varying, country character varying)
RETURNS SETOF geocoding_type AS
$BODY$
DECLARE
        address geocoding_address_type;
BEGIN

                address.street := street;
                address.munsub := munsub;
                address.mun := mun;
                address.subcountry := subcountry;
                address.country := country;
		RETURN QUERY SELECT * FROM gofleet_geocoding(address);
END;
$BODY$
LANGUAGE 'plpgsql';


/* Create a function to return the geometry associated to an address */
CREATE FUNCTION gofleet_geocoding(address_geocoding geocoding_address_type)
RETURNS SETOF geocoding_type AS
$BODY$
DECLARE
	query TEXT;
	res_address geocoding_address_type;
	res geocoding_type;
	rec RECORD;
BEGIN
	/* Create the main query */
	query := 'SELECT s.name as name_street,
			ms.name as name_mun_sub,
			m.name as name_mun,
			cs.name as name_country_sub, 
			c.name as name_country,
			resultado.geometry as geom 
		FROM street as s,
			municipality_subdivision as ms,
			municipality as m,
			country_subdivision as cs,
			country as c,
			replace_table as resultado 
			WHERE replaceme.id = resultado.id';

	select replace(query, 'replace_table', 'street') INTO query;
	select replace(query, 'replaceme', 's') INTO query;

	/* Check the street's name */
	IF(address_geocoding.street IS NOT NULL) THEN
		/* Add sql condition */
		query := query || ' AND s.name ILIKE ';
		query := query || E'\'';
		query := query ||'%'||address_geocoding.street||'%';
		query :=  query || E'\'';
		/* Replace two values of the main query: the name of the main table and his short name */
		select replace(query, 'replace_table', 'street') INTO query;
		select replace(query, 'replaceme', 's') INTO query;
		RAISE INFO 'Consulta: %', query;
	END IF;
	/* Check the municipality subdivision's name */
	IF(address_geocoding.munsub IS NOT NULL) THEN
		query := query || ' AND ms.name ILIKE ';
		query := query || E'\'';
		query := query ||'%'||address_geocoding.munsub||'%';
		query :=  query || E'\'';
		/* Replace two values of the main query: the name of the main table and his short name */
		select replace(query, 'replace_table', 'municipality_subdivision') INTO query;
		select replace(query, 'replaceme', 'ms') INTO query;
		RAISE INFO 'Consulta: %', query;
	END IF;

	/* Check the municipality's name */
	IF(address_geocoding.mun IS NOT NULL) THEN
		query := query || ' AND m.name ILIKE ';
		query := query || E'\'';
		query := query ||'%'||address_geocoding.mun||'%';
		query :=  query || E'\'';
		/* Replace two values of the main query: the name of the main table and his short name */
		select replace(query, 'replace_table', 'municipality') INTO query;
		select replace(query, 'replaceme', 'm') INTO query;
		RAISE INFO 'Consulta: %', query;
	END IF;
	/* Check the country subdivision's name */
	IF(address_geocoding.subcountry IS NOT NULL) THEN
		query := query || ' AND cs.name ILIKE ';
		query := query || E'\'';
		query := query ||'%'||address_geocoding.subcountry||'%';
		query :=  query || E'\'';
		/* Replace two values of the main query: the name of the main table and his short name */
		select replace(query, 'replace_table', 'country_subdivision') INTO query;
		select replace(query, 'replaceme', 'cs') INTO query;
		RAISE INFO 'Consulta: %', query;
	END IF;
	/* Check the country subdivision's name */
	IF(address_geocoding.country IS NOT NULL) THEN
		query := query || ' AND c.name ILIKE ';
		query := query || E'\'';
		query := query ||'%'||address_geocoding.country||'%';
		query :=  query || E'\'';
		/* Replace two values of the main query: the name of the main table and his short name */
		select replace(query, 'replace_table', 'country') INTO query;
		select replace(query, 'replaceme', 'c') INTO query;
		RAISE INFO 'Consulta: %', query;
	END IF;	

	query :=  query || ' AND st_distance(ms.geometry, resultado.geometry) = 0
			AND st_distance(m.geometry, resultado.geometry) = 0
			AND st_distance(cs.geometry, resultado.geometry) = 0
			AND st_distance(c.geometry, resultado.geometry) = 0
			AND st_distance(s.geometry, resultado.geometry) = 0 
			order by st_area(m.geometry) asc limit 1';	
	RAISE INFO 'Consulta: %', query;
	
	FOR rec IN EXECUTE(query) LOOP
	/* Add the results to the type to return */
		res_address.street := rec.name_street;
		res_address.munsub := rec.name_mun_sub;
		res_address.mun := rec.name_mun;
		res_address.subcountry := rec.name_country_sub;
		res_address.country := rec.name_country;
		res.address := res_address;
		res.geometry := rec.geom;
		RETURN NEXT res;
	END LOOP;	
END;
$BODY$
LANGUAGE 'plpgsql';
