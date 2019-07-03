{\rtf1\ansi\ansicpg1252\cocoartf1265\cocoasubrtf210
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx720\tx1440\tx2160\tx2880\tx3600\tx4320\tx5040\tx5760\tx6480\tx7200\tx7920\tx8640\pardirnatural

\f0\fs24 \cf0 -- View: rv_payrollgeneral2\
\
-- DROP VIEW rv_payrollgeneral2;\
\
CREATE OR REPLACE VIEW rv_payrollgeneral2 AS \
 SELECT o.name AS orgemployee,\
    bp.value AS cedula,\
    bp.taxid,\
    ba.a_name AS cuentabanco,\
    max(hr_movement.ad_client_id) AS ad_client_id,\
    max(hr_movement.ad_org_id) AS ad_org_id,\
    max(hr_movement.created) AS created,\
    max(hr_movement.createdby) AS createdby,\
    max(hr_movement.updated) AS updated,\
    max(hr_movement.updatedby) AS updatedby,\
    hr_movement.hr_process_id,\
    hr_movement.c_bpartner_id,\
    bp.name AS nombre,\
    getamountconcept(hr_movement.hr_process_id, 'A_SUELDO_MENSUAL'::character varying, hr_movement.c_bpartner_id) AS a_sueldo_mensual,\
    getamountconcept(hr_movement.hr_process_id, 'A_NOV_HORAS_EXTRAS_SUPLEMENTARIAS'::character varying, hr_movement.c_bpartner_id) AS a_nov_horas_extras_suplement,\
    getamountconcept(hr_movement.hr_process_id, 'A_NOV_HORAS_EXTRAS_NOCTURNAS'::character varying, hr_movement.c_bpartner_id) AS a_nov_horas_extras_nocturnas,\
    getamountconcept(hr_movement.hr_process_id, 'A_NOV_HORAS_EXTRAS_COMPLEMENTARIAS'::character varying, hr_movement.c_bpartner_id) AS a_nov_horas_extras_complement,\
    getamountconcept(hr_movement.hr_process_id, 'A_Dir.Rol.DicReliq'::character varying, hr_movement.c_bpartner_id) AS a_dirroldicreliq,\
    getamountconcept(hr_movement.hr_process_id, 'C_RECARGO_HORAS_SUPLEMENTARIAS'::character varying, hr_movement.c_bpartner_id) AS c_recargo_horas_suplement,\
    getamountconcept(hr_movement.hr_process_id, 'C_RECARGO_HORAS_COMPLE'::character varying, hr_movement.c_bpartner_id) AS c_recargo_horas_comple,\
    getamountconcept(hr_movement.hr_process_id, 'C_RECARGO_HORAS_NOCTURNAS'::character varying, hr_movement.c_bpartner_id) AS c_recargo_horas_nocturnas,\
    getamountconcept(hr_movement.hr_process_id, 'C_SUBSIDIO_HORAS_LUNCH'::character varying, hr_movement.c_bpartner_id) AS c_subsidio_horas_lunch,\
    getamountconcept(hr_movement.hr_process_id, 'CC_SUELDO'::character varying, hr_movement.c_bpartner_id) AS cc_sueldo,\
    getamountconcept(hr_movement.hr_process_id, 'CC_PAGO_TERCER_SUELDO'::character varying, hr_movement.c_bpartner_id) AS cc_pago_tercer_sueldo,\
    getamountconcept(hr_movement.hr_process_id, 'CC_HORAS_EXTRAS_COMPLE'::character varying, hr_movement.c_bpartner_id) AS cc_horas_extras_comple,\
    getamountconcept(hr_movement.hr_process_id, 'CC_HORAS_EXTRAS_SUPLEMENTARIAS'::character varying, hr_movement.c_bpartner_id) AS cc_horas_extras_suplement,\
    getamountconcept(hr_movement.hr_process_id, 'CC_HORAS_EXTRAS_NOCT'::character varying, hr_movement.c_bpartner_id) AS cc_horas_extras_noct,\
    getamountconcept(hr_movement.hr_process_id, 'CC_ALIMENTACION'::character varying, hr_movement.c_bpartner_id) AS cc_alimentacion,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PRESTAMO_HIPOT_IESS'::character varying, hr_movement.c_bpartner_id) AS cc_monto_prestamo_hipot_iess,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PRESTAMO_PERSONAL'::character varying, hr_movement.c_bpartner_id) AS cc_monto_prestamo_personal,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PRESTAMO_QUIROGRAFARIO'::character varying, hr_movement.c_bpartner_id) AS cc_monto_prestamo_quirogra,\
    getamountconcept(hr_movement.hr_process_id, 'CC_APORTE_IESS_PERS'::character varying, hr_movement.c_bpartner_id) AS cc_aporte_iess_pers,\
    getamountconcept(hr_movement.hr_process_id, 'CC_PAGO_FONDO_RESER'::character varying, hr_movement.c_bpartner_id) AS cc_pago_fondo_reser,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PAGAR_COMISIONES'::character varying, hr_movement.c_bpartner_id) AS cc_monto_pagar_comisiones,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PAGAR_MOVILIZACION'::character varying, hr_movement.c_bpartner_id) AS cc_monto_pagar_moviliz,\
    getamountconcept(hr_movement.hr_process_id, 'CC_IMPUESTO_RENTA_CALCULO'::character varying, hr_movement.c_bpartner_id) AS cc_impuesto_renta_calculo,\
    getamountconcept(hr_movement.hr_process_id, 'CC_ASIG_MATERNIDAD'::character varying, hr_movement.c_bpartner_id) AS cc_asig_maternidad,\
    getamountconcept(hr_movement.hr_process_id, 'CC_AYUDA_SOCIAL'::character varying, hr_movement.c_bpartner_id) AS cc_ayuda_social,\
    getamountconcept(hr_movement.hr_process_id, 'CC_SUELDO_MENSUAL'::character varying, hr_movement.c_bpartner_id) AS cc_sueldo_mensual,\
    getamountconcept(hr_movement.hr_process_id, 'CC_ANTICIPO_QUINCENA'::character varying, hr_movement.c_bpartner_id) AS cc_anticipo_quincena,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PAGAR_RELIQUIDACION'::character varying, hr_movement.c_bpartner_id) AS cc_monto_pagar_reliq,\
    getamountconcept(hr_movement.hr_process_id, 'CC_SUPERMERCADO'::character varying, hr_movement.c_bpartner_id) AS cc_supermercado,\
    getamountconcept(hr_movement.hr_process_id, 'CC_CELULARES'::character varying, hr_movement.c_bpartner_id) AS cc_celulares,\
    getamountconcept(hr_movement.hr_process_id, 'CC_DEDUCCION_ASISTENCIA_MEDICA'::character varying, hr_movement.c_bpartner_id) AS cc_deduccion_asistencia_m,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_SEGURO_VEHICULO'::character varying, hr_movement.c_bpartner_id) AS cc_monto_seguro_vehiculo,\
    getamountconcept(hr_movement.hr_process_id, 'CC_DEDUCCION_ATRASOS'::character varying, hr_movement.c_bpartner_id) AS cc_deduccion_atrasos,\
    getamountconcept(hr_movement.hr_process_id, 'A_HORAS_EXTRAORDINARIAS'::character varying, hr_movement.c_bpartner_id) AS a_horas_extraordinarias,\
    getamountconcept(hr_movement.hr_process_id, 'CC_HORAS_EXTRAORDINARIAS'::character varying, hr_movement.c_bpartner_id) AS cc_horas_extraordinarias,\
    getamountconcept(hr_movement.hr_process_id, 'CC_TOTAL_DIAS_LABORADOS'::character varying, hr_movement.c_bpartner_id) AS cc_total_dias_laborados,\
    getamountconcept(hr_movement.hr_process_id, 'CC_DEV_CEL'::character varying, hr_movement.c_bpartner_id) AS cc_dev_cel,\
    getamountconcept(hr_movement.hr_process_id, 'D_DILIPA'::character varying, hr_movement.c_bpartner_id) AS d_dilipa,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_PRESTAMO_QUIROGRAFARIO'::character varying, hr_movement.c_bpartner_id) AS d_cuota_prestamo_quirogra,\
    getamountconcept(hr_movement.hr_process_id, 'D_DIAS_DEDUCCION_INASISTENCIA'::character varying, hr_movement.c_bpartner_id) AS bont,\
    getamountconcept(hr_movement.hr_process_id, 'D_HORAS_FALTAS'::character varying, hr_movement.c_bpartner_id) AS d_horas_faltas,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_PRESTAMO_PERSONAL'::character varying, hr_movement.c_bpartner_id) AS d_cuota_prestamo_personal,\
    getamountconcept(hr_movement.hr_process_id, 'D_HORAS_ATRASOS_MINUTOS'::character varying, hr_movement.c_bpartner_id) AS d_horas_atrasos_minutos,\
    getamountconcept(hr_movement.hr_process_id, 'D_MONTO_MANUNTENCION'::character varying, hr_movement.c_bpartner_id) AS d_monto_manuntencion,\
    getamountconcept(hr_movement.hr_process_id, 'D_EXTENSION_SALUD'::character varying, hr_movement.c_bpartner_id) AS d_extension_salud,\
    getamountconcept(hr_movement.hr_process_id, 'D_ASISTENCIA_MEDICA'::character varying, hr_movement.c_bpartner_id) AS d_asistencia_medica,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_PRESTAMO_HIPOTECARIO'::character varying, hr_movement.c_bpartner_id) AS d_cuota_prestamo_hipo,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_OTRAS_ASO'::character varying, hr_movement.c_bpartner_id) AS d_cuota_otras_aso,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_OTRAS_SINDICATO'::character varying, hr_movement.c_bpartner_id) AS d_cuota_otras_sindicato,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_OTRAS_VARIAS'::character varying, hr_movement.c_bpartner_id) AS d_cuota_otras_varias,\
    getamountconcept(hr_movement.hr_process_id, 'D_OTROS_DESCUENTOS'::character varying, hr_movement.c_bpartner_id) AS d_otros_descuentos,\
    getamountconcept(hr_movement.hr_process_id, 'D_HORAS_EGRESO'::character varying, hr_movement.c_bpartner_id) AS d_horas_egreso,\
    getamountconcept(hr_movement.hr_process_id, 'D_HORAS_INGRESO'::character varying, hr_movement.c_bpartner_id) AS d_horas_ingreso,\
    getamountconcept(hr_movement.hr_process_id, 'D_ACUM_DIAS_VACACIONES_GOZADAS'::character varying, hr_movement.c_bpartner_id) AS d_acum_dias_vacaciones_g,\
    getamountconcept(hr_movement.hr_process_id, 'D_ACUM_DIAS_PERMISO_CARGO_VACACION'::character varying, hr_movement.c_bpartner_id) AS d_acum_dias_permiso_cargo_va,\
    getamountconcept(hr_movement.hr_process_id, 'D_DIAS_PERMISO_CARGO_VACACION'::character varying, hr_movement.c_bpartner_id) AS d_dias_permiso_cargo_vacacion,\
    getamountconcept(hr_movement.hr_process_id, 'D_SUPERMERCADO'::character varying, hr_movement.c_bpartner_id) AS d_supermercado,\
    getamountconcept(hr_movement.hr_process_id, 'D_AYUDA_SOCIAL'::character varying, hr_movement.c_bpartner_id) AS d_ayuda_social,\
    getamountconcept(hr_movement.hr_process_id, 'D_LAPTOP'::character varying, hr_movement.c_bpartner_id) AS d_laptop,\
    getamountconcept(hr_movement.hr_process_id, 'D_CELULARES'::character varying, hr_movement.c_bpartner_id) AS d_celulares,\
    getamountconcept(hr_movement.hr_process_id, 'D_RET_JUD'::character varying, hr_movement.c_bpartner_id) AS d_ret_jud,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_PRESTAMO_HIP_IESS'::character varying, hr_movement.c_bpartner_id) AS d_cuota_prestamo_hip_iess,\
    getamountconcept(hr_movement.hr_process_id, 'D_ALIMENTACION'::character varying, hr_movement.c_bpartner_id) AS d_alimentacion,\
    getamountconcept(hr_movement.hr_process_id, 'D_SEGURO_VEHICULO'::character varying, hr_movement.c_bpartner_id) AS d_seguro_vehiculo,\
    getamountconcept(hr_movement.hr_process_id, 'D_MULTA'::character varying, hr_movement.c_bpartner_id) AS d_multa,\
    getamountconcept(hr_movement.hr_process_id, 'D_FYBECA'::character varying, hr_movement.c_bpartner_id) AS d_fybeca,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_PRESTAMO_PERSONAL2'::character varying, hr_movement.c_bpartner_id) AS d_cuota_prestamo_personal2,\
    getamountconcept(hr_movement.hr_process_id, 'D_BONIFICACIONES'::character varying, hr_movement.c_bpartner_id) AS d_bonificaciones,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_OTRAS_VARIAS2'::character varying, hr_movement.c_bpartner_id) AS d_cuota_otras_varias2,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_OTRAS_VARIAS3'::character varying, hr_movement.c_bpartner_id) AS d_cuota_otras_varias3,\
    getamountconcept(hr_movement.hr_process_id, 'D_CUOTA_OTRAS_VARIAS4'::character varying, hr_movement.c_bpartner_id) AS d_cuota_otras_varias4,\
    getamountconcept(hr_movement.hr_process_id, 'D_DIF_ROL'::character varying, hr_movement.c_bpartner_id) AS d_dif_rol,\
    ( SELECT sum(\
                CASE\
                    WHEN c.ispaid = 'Y'::bpchar AND c.isprinted = 'Y'::bpchar AND m.accountsign <> 'C'::bpchar THEN round(m.amount, 2)\
                    WHEN c.ispaid = 'Y'::bpchar AND c.isprinted = 'Y'::bpchar AND m.accountsign = 'C'::bpchar THEN round(m.amount, 2) * (- 1::numeric)\
                    ELSE 0::numeric\
                END) AS netamt\
           FROM hr_movement m\
             JOIN hr_concept c ON c.hr_concept_id = m.hr_concept_id\
          WHERE m.hr_process_id = hr_movement.hr_process_id AND m.c_bpartner_id = hr_movement.c_bpartner_id) AS totalpagar,\
    e.c_activity_id,\
    getamountconcept(hr_movement.hr_process_id, 'CC_TOTAL_REMUNERACION_APORTABLE'::character varying, hr_movement.c_bpartner_id) AS cc_total_remuneracion_a,\
    getamountconcept(hr_movement.hr_process_id, 'CC_PROVISION_TERCER_SUELDO'::character varying, hr_movement.c_bpartner_id) AS cc_provision_tercer_sueldo,\
    getamountconcept(hr_movement.hr_process_id, 'CC_PROVISION_CUARTO_SUELDO'::character varying, hr_movement.c_bpartner_id) AS cc_provision_cuarto_sueldo,\
    getamountconcept(hr_movement.hr_process_id, 'CC_PROVISION_FONDO_RESER'::character varying, hr_movement.c_bpartner_id) AS cc_provision_fondo_reser,\
    getamountconcept(hr_movement.hr_process_id, 'CC_PROVISION_IMPUESTO_RENTA'::character varying, hr_movement.c_bpartner_id) AS cc_provision_impuesto_renta,\
    getamountconcept(hr_movement.hr_process_id, 'CC_PROVISION_VACACIONES'::character varying, hr_movement.c_bpartner_id) AS cc_provision_vacaciones,\
    getamountconcept(hr_movement.hr_process_id, 'CC_PROVISION_IEC'::character varying, hr_movement.c_bpartner_id) AS cc_provision_iec,\
    getamountconcept(hr_movement.hr_process_id, 'CC_PROVISION_SECAP'::character varying, hr_movement.c_bpartner_id) AS cc_provision_secap,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PRESTAMO_PERSONALA'::character varying, hr_movement.c_bpartner_id) AS cc_monto_prestamo_personala,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PRESTAMO_HIPOTECARIO'::character varying, hr_movement.c_bpartner_id) AS cc_monto_prestamo_hipotecario,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PRESTAMO_PERSONAL2'::character varying, hr_movement.c_bpartner_id) AS cc_monto_prestamo_personal2,\
    getamountconcept(hr_movement.hr_process_id, 'CC_DEDUCCION_FALTAS'::character varying, hr_movement.c_bpartner_id) AS cc_deduccion_faltas,\
    getamountconcept(hr_movement.hr_process_id, 'CC_ASIGNACION_INASISTENCIA'::character varying, hr_movement.c_bpartner_id) AS cc_asignacion_inasistencia,\
    getamountconcept(hr_movement.hr_process_id, 'CC_DEDUCCION_ATRASOS'::character varying, hr_movement.c_bpartner_id) AS cc_deduccion_atraso,\
    getamountconcept(hr_movement.hr_process_id, 'CC_DEDUCCION_INASISTENCIA'::character varying, hr_movement.c_bpartner_id) AS cc_deduccion_inasistencia,\
    getamountconcept(hr_movement.hr_process_id, 'CC_DEDUCCION_FYBECA'::character varying, hr_movement.c_bpartner_id) AS cc_deduccion_fybeca,\
    getamountconcept(hr_movement.hr_process_id, 'CC_LAPTOP'::character varying, hr_movement.c_bpartner_id) AS cc_laptop,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_OTRAS_VARIAS'::character varying, hr_movement.c_bpartner_id) AS cc_monto_otras_varias,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_OTRAS_VARIAS_B'::character varying, hr_movement.c_bpartner_id) AS cc_monto_otras_varias_b,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_OTRAS_VARIAS_C'::character varying, hr_movement.c_bpartner_id) AS cc_monto_otras_varias_c,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_OTRAS_VARIAS_D'::character varying, hr_movement.c_bpartner_id) AS cc_monto_otras_varias_d,\
    getamountconcept(hr_movement.hr_process_id, 'CC_DIF_ROL'::character varying, hr_movement.c_bpartner_id) AS cc_dif_rol,\
    ( SELECT sum(\
                CASE\
                    WHEN c.ispaid = 'Y'::bpchar AND c.isprinted = 'Y'::bpchar AND m.accountsign <> 'C'::bpchar THEN round(m.amount, 2)\
                    ELSE 0::numeric\
                END) AS netamt\
           FROM hr_movement m\
             JOIN hr_concept c ON c.hr_concept_id = m.hr_concept_id\
          WHERE m.hr_process_id = hr_movement.hr_process_id AND m.c_bpartner_id = hr_movement.c_bpartner_id) AS totalingresos,\
    ( SELECT sum(\
                CASE\
                    WHEN c.ispaid = 'Y'::bpchar AND c.isprinted = 'Y'::bpchar AND m.accountsign = 'C'::bpchar THEN round(m.amount, 2)\
                    ELSE 0::numeric\
                END) AS netamt\
           FROM hr_movement m\
             JOIN hr_concept c ON c.hr_concept_id = m.hr_concept_id\
          WHERE m.hr_process_id = hr_movement.hr_process_id AND m.c_bpartner_id = hr_movement.c_bpartner_id) AS totalegresos,\
    getamountconcept(hr_movement.hr_process_id, 'CC_APORTE_IESS_PATRONAL'::character varying, hr_movement.c_bpartner_id) AS cc_aporte_iess_patronal,\
    getamountconcept(hr_movement.hr_process_id, 'C_RET_JUD'::character varying, hr_movement.c_bpartner_id) AS c_ret_jud,\
    getamountconcept(hr_movement.hr_process_id, 'CC_RET_JUD'::character varying, hr_movement.c_bpartner_id) AS cc_ret_jud,\
    getamountconcept(hr_movement.hr_process_id, 'CC_QUINCENA'::character varying, hr_movement.c_bpartner_id) AS cc_quincena,\
    getamountconcept(hr_movement.hr_process_id, 'CC_DEDUCCION_DESC_OTROS'::character varying, hr_movement.c_bpartner_id) AS cc_deduccion_desc_otros,\
    getamountconcept(hr_movement.hr_process_id, 'CC_BONIFICACIONES'::character varying, hr_movement.c_bpartner_id) AS cc_bonificaciones,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PAGAR_RELIQUIDACION'::character varying, hr_movement.c_bpartner_id) AS cc_reliquidacion_sueldo,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_PAGAR_RELIQ_HORAS_EXTRA'::character varying, hr_movement.c_bpartner_id) AS cc_reliquidacion_horas,\
    getamountconcept(hr_movement.hr_process_id, 'CC_TOTAL_DIAS_LABORADOS_MENSUAL'::character varying, hr_movement.c_bpartner_id) AS diaslaboradosmensual,\
    getamountconcept(hr_movement.hr_process_id, 'CC_INGRESOS_NO_APORTABLES'::character varying, hr_movement.c_bpartner_id) AS cc_ingresos_no_aportables,\
    getamountconcept(hr_movement.hr_process_id, 'CC_INGRESOS_NO_APORTABLES_IND'::character varying, hr_movement.c_bpartner_id) AS cc_ingresos_no_aportables_ind,\
    getamountconcept(hr_movement.hr_process_id, 'CC_TOTAL_DEDUCCION'::character varying, hr_movement.c_bpartner_id) AS cc_total_deduccion,\
    getamountconcept(hr_movement.hr_process_id, 'CC_TOTAL_ASIGNACION-DEDUCCION'::character varying, hr_movement.c_bpartner_id) AS cc_total_asignacion_deduccion,\
    getamountconcept(hr_movement.hr_process_id, 'CC_MONTO_DEDUCIR_DIAS_INGRESO'::character varying, hr_movement.c_bpartner_id) AS cc_monto_deducir_dias_ingreso,\
    getamountconcept(hr_movement.hr_process_id, 'CC_DILIPA'::character varying, hr_movement.c_bpartner_id) AS CC_DILIPA\
   FROM hr_movement\
     JOIN c_bpartner bp ON hr_movement.c_bpartner_id = bp.c_bpartner_id\
     LEFT JOIN hr_employee e ON bp.c_bpartner_id = e.c_bpartner_id\
     LEFT JOIN hr_process pp ON pp.hr_process_id = hr_movement.hr_process_id\
     LEFT JOIN ad_org o ON o.ad_org_id = e.ad_org_id\
     LEFT JOIN c_bp_bankaccount ba ON ba.c_bpartner_id = e.c_bpartner_id\
  WHERE hr_movement.ad_client_id = 1000002::numeric AND e.isactive = 'Y'::bpchar AND e.ad_org_id = pp.ad_org_id\
  GROUP BY hr_movement.hr_process_id, bp.name, hr_movement.c_bpartner_id, o.name, bp.value, ba.a_name, bp.taxid, e.c_activity_id;\
\
ALTER TABLE rv_payrollgeneral2\
  OWNER TO adempiere;}