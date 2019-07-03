CREATE OR REPLACE FUNCTION adempiere.getasumconceptbysign (in phr_process_id numeric, in pc_bpartner_id numeric,in paccountsign character varying) RETURNS numeric AS
'
/*************************************************************************
 *
 ************************************************************************/
DECLARE
	v_amount		hr_movement.amount%TYPE;	

BEGIN
	--	Get  amount or qty
		SELECT	SUM(round(m.amount,2))::numeric AS amount
		INTO	v_amount
		FROM hr_movement m
        JOIN HR_Process p ON m.HR_Process_ID=p.HR_Process_ID
        JOIN HR_Concept c ON m.HR_Concept_ID=c.HR_Concept_ID
        JOIN C_BPartner bp ON m.C_BPartner_ID=bp.C_BPartner_ID
        WHERE
		(p.hr_process_id = phr_process_id)
        AND (m.C_BPartner_ID =  pc_bpartner_id )
        AND c.isprinted = ''Y''
        AND c.ispaid = ''Y''
        AND (COALESCE(m.amount,0) <>0  OR COALESCE(m.qty,0)<>0 )
        AND (m.accountsign <> null or m.accountsign <> '''' )
        AND m.accountsign = paccountsign;

    IF v_amount IS NULL THEN
        v_amount = 0;
    END IF;

	RETURN	v_amount;
END;

'
LANGUAGE 'plpgsql'
GO
