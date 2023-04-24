
-- CONTAR DOCS E REGISTROS
SELECT COUNT(DISTINCT(doc)) AS docs, COUNT(*) AS regs
FROM contents
-- 14/07/17: 34655;2503634

SELECT (SELECT COUNT(*) FROM contents) AS triplas, (SELECT COUNT(*) from links) AS links
-- 17/07/17 2511318;253024


-- VERIFICAR O MAIOR CONTEUDO (TAMANHO DO TEXTO)
select max(length(content)) from contents
-- 3993

-- VERIFICAR O DOC COM O MAIOR TEXTO
select * from contents
WHERE length(content) = 3993
order by DOC, line

-- VERIFICAR OS LINKS DE L4881A
select *
-- delete
from contenTs
where doc = 'L4881A'
and type = 'Link'
order by line


-- VERIFICAR OS LINKS DO CODIGO CIVIL
select *
-- delete
from contenTs
where doc = 'L10406'
and type = 'Link'
order by line


-- PEGAR DOC, LINE COM CONTENT MAL FORMADO POR EXEMPLO VÁRIOS Art Art ...
SELECT * FROM (
SELECT doc, LINE, CONTENT, (SELECT count(*) FROM regexp_matches(CONTENT, 'Art ', 'g')) AS reps
FROM   contenTs
) t
WHERE T.reps > 1
order by doc, line

-- VÁRIOS Art. Art. ...  Obs. nem todos estão errados ...
SELECT * FROM (
SELECT doc, LINE, CONTENT, (SELECT count(*) FROM regexp_matches(CONTENT, 'Art. ', 'g')) AS reps
FROM   contenTs
) t
WHERE T.reps > 1
order by doc, line


-- PEGAR SUBNIVEIS DE ARTIGO
-- http://ita.br/lex/D0075/ParteInicial/Artigo1
SELECT DISTINCT type FROM (
SELECT    CASE 
            WHEN t.pos = 0 THEN t.name 
            ELSE SUBSTRING(t.parent, t.pos, t.len-t.pos+1) || '/' || t.name
          END AS estrut,
          *
FROM (
  SELECT    POSITION('Artigo' IN parent) AS pos, LENGTH(parent) AS len,
            * 
  FROM      contents         
  WHERE   ((parent ILIKE '%artigo%') OR (type ILIKE 'Artigo'))
  AND       NOT type = 'Link'
  AND       type = 'Capitulo'
  --ORDER BY doc, line
) t
) u
 ORDER BY 1

  Alinea
Anexo
Artigo
Capitulo
Caput
Fecho
Inciso
Item
Linha
Livro
Paragrafo
Parte
ParteFinal
Pena
Secao
Subsecao
Titulo


-- VERIFICAR OS LINKS DE L4881A
select *
from contenTs
where doc = 'D6417'
--and type = 'Link'
order by doc, line

select *
from contenTs
where doc = 'L13105'
--and type = 'Link'
order by doc, line

-- drop VIEW links_view ;

CREATE OR REPLACE VIEW links_view AS
SELECT  *, 
        SPLIT_PART(link, '#', 1) AS  to_doc, 
        SPLIT_PART(SPLIT_PART(link, '#', 2), '§', 1) AS to_art, 
        SUBSTRING(REPLACE(REPLACE(REPLACE(REPLACE(SPLIT_PART(SPLIT_PART(link, '#', 2), '§', 2),'.', ''), ',', ''), '-', ''), 'º', '') FROM '[0-9]+')  AS to_par
FROM    links
;



SELECT * FROM links_view
WHERE content ilike '%regul%'
ORDER BY doc, line
;



select *
from contents
where doc ILIKE 'E%'
ORDER BY doc, line

SELECT doc, line, link
FROM   links
WHERE link ILIKE '%ecr%'
ORDER BY doc, line

-- SELECT DISTINCT name FROM contents WHERE name ILIKE 'Paragrafo%'


SELECT a
FROM unnest(string_to_array('john,smith,jones', ',')) AS a;


select * from contents
where name ilike '%nic%'
and type = 'Paragrafo'
ORDER BY doc, line


SELECT * from contents 
WHERE  Parent = 'D1808/Artigo25'
AND    name = 'Paragrafo9'
ORDER BY doc, line

select max(length(name)) from contents



INSERT INTO contents ( line, doc, level, type, parent, name, content ) 
 VALUES (   43, 'Constituicao24',  009, 'Artigo', 'Constituicao24', 'Artigo15', '        Art. 15. E´ da attribuição da Assembléa Geral' )

    
    
SELECT * FROM CONTENTS WHERE DOC = 'L5869consol'
ORDER BY doc, line

-- DELETE FROM CONTENTS WHERE DOC = 'D0084'; DELETE FROM LINKS WHERE DOC = 'D0084';



SELECT      * 
FROM        Links 
WHERE       link ILIKE '%9609%' 
ORDER BY doc, line


SELECT      * 
FROM        contents 
WHERE       parent ilike '%L9609%'
ORDER BY doc, line


INSERT INTO contents ( line, doc, level, type, parent, name, content )  
VALUES (   77, 'LIM3150',  012, 'Alinea', 'LIM3150/Artigo11', 'AlineaA'', '    a) A´ sociedade pela negligencia, culpa ou dólo com que se houverem no desempenho do mandato;' )


-- DocLegal
SELECT 'CREATE (' || doc || ':DocLegal {title:' || CHR(39) || doc || CHR(39) || '})' AS node, *
FROM   Contents
WHERE  doc IN ('D2556', 'L9609', 'L9610', 'L7646', 'L5869')
AND    type = 'DocLegal'
ORDER BY doc, line

-- Others
SELECT DISTINCT 'CREATE (' || REPLACE(REPLACE(parent, '/', '_'), '-', '_') || '_' || 
	REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(name, '(', ''), ')', ''), ';', ''), ',', ''), '-', '_') || ':' || type || ' {title:' || CHR(39) || 
	REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(name, '(', ''), ')', ''), ';', ''), ',', ''), '-', '_') || CHR(39) || ', text:' || CHR(39) || content || CHR(39) || '})' AS node
	--, *
FROM   Contents
WHERE  doc IN ('D2556', 'L9609', 'L9610' ) -- , 'L7646', 'L5869')
AND    NOT type = 'DocLegal'
ORDER BY 1 --doc, line
LIMIT 300
OFFSET 0



SELECT COUNT(*) FROM contents


SELECT * 
FROM   contents
WHERE  /*type = 'Texto'
AND    */content ILIKE '%uso de programa de computador%'


DROP VIEW contents_view ;


CREATE OR REPLACE VIEW contents_view AS
SELECT   TRANSLATE(parent,'0123456789', '') AS pattern, *
FROM     Contents
;

SELECT DISTINCT pattern 
FROM   contents_view 
WHERE  NOT doc IN (
	SELECT    DISTINCT doc 
	FROM      contents_view 
	WHERE     pattern LIKE '%/Artigo/Artigo/Artigo/Artigo/Artigo/Artigo/%'
)
ORDER BY 1


SELECT    DISTINCT doc 
FROM      contents_view 
WHERE     pattern LIKE '%/Artigo/Artigo/Artigo/Artigo/Artigo/Artigo/%'
ORDER BY  doc --, line
/* A maionese desandou nestes docs */
Constituicao
Constituicao34
Constituicao46
Constituicao67EMC69
Constituicaoconsol
D20865
D4805
D5078
D5731
D6860
D7462
D7688
Del3689
L1506
L7664
L7664consol


SELECT COUNT(*) FROM triples

-- DELETE FROM triples;


SELECT     * 
FROM       triples

ORDER BY   2, 3, 4


CREATE TABLE triplicates AS
SELECT sub, pre, obj
         FROM   triples
         GROUP BY sub, pre, obj
         HAVING COUNT(*) > 1;

SELECT COUNT(*) FROM triplicates;

SELECT *
FROM   triples t
JOIN   triplicates u ON  u.sub = t.sub 
                     AND u.pre = t.pre 
                     AND u.obj = t.obj
;



SELECT     id 
FROM       triples
WHERE      pre = 'eh_regulado_por'


-- inicial das triplas que começam por / (barra)
------------------------------------------------
SELECT   DISTINCT SUBSTRING(sub, 1, 8)
FROM     triples
WHERE    sub LIKE '/%'
ORDER BY 1
/L11355a
/L11357a
/L12702a
/L12778a


-- inicial das triplas em triplicates
------------------------------------------------
SELECT   DISTINCT SUBSTRING(sub, 1, 8)
FROM triplicates
ORDER BY 1



-- analise dos predicados Object Property
-----------------------------------------
SELECT    DISTINCT pre
FROM      triples
ORDER BY  1       
;
eh_um
eh_regulado_por
foi_revogado_por
regulamenta
remete_para
revogou
tem_alinea
tem_anexo
tem_artigo
tem_capitulo
tem_caput
tem_ementa
tem_fecho
tem_inciso
tem_item
tem_livro
tem_paragrafo
tem_parte
tem_parte_final
tem_parte_inicial
tem_pena
tem_preambulo
tem_secao
tem_subitulo
tem_subsecao
tem_texto
tem_titulo


SELECT DISTINCT obj
FROM   triples 
WHERE  pre = 'eh_um'
AND    sub IN 
( SELECT DISTINCT sub
  FROM   triples
  WHERE  pre = 'tem_caput'
) 
ORDER BY 1

SELECT     *
FROM       triples 
WHERE      obj ILIKE 'subit%' 
ORDER BY   1

UPDATE triples SET obj = 'Subtitulo' WHERE obj = 'Subitulo'

SELECT    DISTINCT pre 
FROM      triples
ORDER BY 1

UPDATE triples SET pre = 'tem_subtitulo' WHERE pre = 'tem_subitulo'


SELECT   'tem' || INITCAP(SPLIT_PART(pre, '_', 2)) FROM  -- 
UPDATE   triples 
SET      pre = 'tem' || INITCAP(SPLIT_PART(pre, '_', 2))
WHERE    pre LIKE 'tem%'

UPDATE triples SET pre = 'regulamentadoPor' WHERE pre = 'eh_regulado_por';
UPDATE triples SET pre = 'ehUm'             WHERE pre = 'eh_um';
UPDATE triples SET pre = 'revogadoPor'      WHERE pre = 'foi_revogado_por';
-- UPDATE triples SET pre = 'regulamenta'   WHERE pre = 'regulamenta';
UPDATE triples SET pre = 'remetePara'       WHERE pre = 'remete_para';
UPDATE triples SET pre = 'revogou'          WHERE pre = 'revogou';
UPDATE triples SET pre = 'temAlinea'        WHERE pre = 'tem_alinea';
UPDATE triples SET pre = 'temAnexo'         WHERE pre = 'tem_anexo';
UPDATE triples SET pre = 'temArtigo'        WHERE pre = 'tem_artigo';
UPDATE triples SET pre = 'temCapitulo'      WHERE pre = 'tem_capitulo';
UPDATE triples SET pre = 'temCaput'         WHERE pre = 'tem_caput';
UPDATE triples SET pre = 'temEmenta'        WHERE pre = 'tem_ementa';
UPDATE triples SET pre = 'temFecho'         WHERE pre = 'tem_fecho';
UPDATE triples SET pre = 'temInciso'        WHERE pre = 'tem_inciso';
UPDATE triples SET pre = 'temItem'          WHERE pre = 'tem_item';
UPDATE triples SET pre = 'temLivro'         WHERE pre = 'tem_livro';
UPDATE triples SET pre = 'temParagrafo'     WHERE pre = 'tem_paragrafo';
UPDATE triples SET pre = 'temParte'         WHERE pre = 'tem_parte';
UPDATE triples SET pre = 'temParteFinal'    WHERE pre = 'tem_parte_final';
UPDATE triples SET pre = 'temParteInicial'  WHERE pre = 'tem_parte_inicial';
UPDATE triples SET pre = 'temPena'          WHERE pre = 'tem_pena';
UPDATE triples SET pre = 'temPreambulo'     WHERE pre = 'tem_preambulo';
UPDATE triples SET pre = 'temSecao'         WHERE pre = 'tem_secao';
UPDATE triples SET pre = 'temSubsecao'      WHERE pre = 'tem_subsecao';
UPDATE triples SET pre = 'temSubtitulo'     WHERE pre = 'tem_subtitulo';
UPDATE triples SET pre = 'temTexto'         WHERE pre = 'tem_texto';
-- Query returned successfully: 5315478 rows affected, 7660715 ms execution time.
UPDATE triples SET pre = 'temTitulo'        WHERE pre = 'tem_titulo';




SELECT DISTINCT pre FROM triples ORDER BY 1;



SELECT    'lex:' || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(sub), '/', '\/') , ',', ''), ';', ''), '(', ''), ')', ''), '$', 'S'), '.', ''), '=', ''), 'º', ''), '|', ''), '[', ''), ']', ''), '<', ''), '>', ''), '*', ''), '!', '')
          || '    ' || 'lex:' || TRIM(pre)  || '    ' ||  
          CASE 
             WHEN pre = 'temTexto' 
             THEN CHR(34) || REPLACE(REPLACE(TRIM(obj), '\', ''), CHR(34), '\' || CHR(34)) || CHR(34)
             ELSE 'lex:'  || REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(obj), '/', '\/') , ',', ''), ';', ''), '(', ''), ')', ''), '$', 'S'), '.', ''), '=', ''), 'º', ''), '|', ''), 'L8212consart95§2', 'L8212consol\/Artigo95\/Paragrafo2'), '[', ''), ']', ''), '<', ''), '>', ''), '*', ''), '!', '')
          END || ' .' AS tripla --, 
          -- *
FROM      triples
WHERE     id IN (
  SELECT  id
  FROM    triples
  ORDER BY  id
  LIMIT     1000000
  OFFSET   10000000
)

-- rode a query acima mudando o OFFSET 0, 1000000, ...
-- exporte para csv sem headers, sem quotings, UTF8, numere os arqs sequencialmente
-- adicione os prefixos ao arquivo gerado antes de carregar no fuseki.


-- EXCEDE 16000 CARACTERES NA COLUNA - testar com tipo TEXT ???
---------------------------------------------------------------
lex:D0399\/Artigo2    lex:temTexto    "Art. 2º A área indígena de que trata
Solucao: Aumentar a configuracao de max caracteres por coluna no PgAdminIII


TRATAR OS CASO ABAIXO
--------------------
lex:D3000\/Artigo617    lex:remetePara    lex:L8212consart95§2 .
e outros:
lex:L10836Sart2§12i
lex:L10836Sart2§12ii 
lex:L10836Sart2§12iii
lex:L10833art58f§3
lex:L7150art1§2
lex:Del0227art90§1
lex:L9082art49§4
lex:Del9797art672§1
lex:Lcp101art9§5
lex:Lcp101art4§3
lex:Lcp101art42
lex:L4771§6
lex:L9615consolart5§3
lex:L9615consolart5
lex:mailto:L8069@art243
lex:mailto:L6830
lex:mailto:L8167
lex:§L9008\/Artigo1\/Paragrafo1
lex:L8383art21§4
lex:L4345@art10
