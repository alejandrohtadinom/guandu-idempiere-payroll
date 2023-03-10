/*
*  Author: Alejandro H Tadino M
*  Name: LVE Liquor Taxes DB
*/


-- Elimina la talba si ya existe
DROP TABLE IF EXISTS LVE_HR_ProcessReport;
-- Crea una tabla nueva
CREATE TABLE IF NOT EXISTS LVE_HR_ProcessReport (
    -- Identificacion de la tabla
    LVE_HR_ProcessReport_ID NUMERIC(10, 0) PRIMARY KEY,
    LVE_HR_ProcessReport_UU CHARACTER VARYING(36) DEFAULT NULL::CHARACTER VARYING UNIQUE,
    -- Estos campos son requistos del sistema
    AD_Client_ID numeric(10, 0) not null,
    AD_Org_ID numeric(10, 0) not null,
    IsActive character(1) not null default 'Y'::bpchar,
    Created timestamp without time zone not null default now(),
    CreatedBy numeric(10, 0) not null,
    Updated timestamp without time zone not null default now(),
    UpdatedBy numeric(10, 0) not null,
    -- Elementos de la herrameinta
    Description CHARACTER VARYING(255),
    FileExportClass CHARACTER VARYING(255),
    IsFileExport CHARACTER(1),
    IsOrderBy CHARACTER(1),
    Name CHARACTER VARYING(255),
    PrintName CHARACTER VARYING(255),
    Processing CHARACTER(1),
    ReceiptFooterMsg CHARACTER VARYING(255)
);

-- Elimina la talba si ya existe
DROP TABLE IF EXISTS LVE_HR_ProcessReportLine;
-- Crea una tabla nueva
CREATE TABLE IF NOT EXISTS LVE_HR_ProcessReportLine (
    -- Identificacion de la tabla
    --
    LVE_HR_ProcessReportLine_ID NUMERIC(10, 0) PRIMARY KEY,
    LVE_HR_ProcessReportLine_UU CHARACTER VARYING(36) DEFAULT NULL::CHARACTER VARYING UNIQUE,
    -- Estos campos son requistos del sistema
    AD_Client_ID NUMERIC(10, 0) NOT NULL,
    AD_Org_ID NUMERIC(10, 0) NOT NULL,
    IsActive CHARACTER(1) NOT NULL DEFAULT 'Y'::BPCHAR,
    Created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CreatedBy NUMERIC(10, 0) NOT NULL,
    Updated TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    UpdatedBy NUMERIC(10, 0) NOT NULL,
    -- Elementos de la herrameinta
    HR_Concept_ID NUMERIC(10, 0) NOT NULL,
    LVE_HR_ProcessReport_ID NUMERIC(10, 0) NOT NULL,
    IsOrderBy CHARACTER(1) NOT NULL DEFAULT 'Y'::BPCHAR,
    PrintName CHARACTER VARYING(255),
    SeqNo NUMERIC(10, 0) NOT NULL
);

-- Elimina la talba si ya existe
DROP TABLE IF EXISTS LVE_RV_HR_ProcessDetail;
-- Crea una tabla nueva
CREATE TABLE IF NOT EXISTS LVE_RV_HR_ProcessDetail (
    -- Identificacion de la tabla
    LVE_RV_HR_ProcessDetail_ID NUMERIC(10, 0) PRIMARY KEY,
    LVE_RV_HR_ProcessDetail_UU CHARACTER VARYING(36) DEFAULT NULL::CHARACTER VARYING UNIQUE,
    -- Estos campos son requistos del sistema
    AD_Client_ID NUMERIC(10, 0) NOT NULL,
    AD_Org_ID NUMERIC(10, 0) NOT NULL,
    IsActive CHARACTER(1) NOT NULL DEFAULT 'Y'::BPCHAR,
    Created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CreatedBy NUMERIC(10, 0) NOT NULL,
    Updated TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    UpdatedBy NUMERIC(10, 0) NOT NULL,
    -- Elementos de la herrameinta
    Amt NUMERIC(22, 2) NOT NULL,
    BPName CHARACTER(255) NOT NULL,
    BPTaxID CHARACTER(255) NOT NULL,
    CategoryValue CHARACTER VARYING(255) NOT NULL,
    C_BPartner_ID NUMERIC(10, 0) NOT NULL,
    ColumnType CHARACTER(1) NOT NULL DEFAULT 'Y'::BPCHAR,
    DateAcct TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    DocumentNo CHARACTER VARYING(255) NOT NULL,
    DocumentNote CHARACTER VARYING(255) NOT NULL,
    HeaderPrintName CHARACTER VARYING(255),
    HR_Concept_Category_ID NUMERIC(10, 0) NOT NULL,
    HR_Concept_ID NUMERIC(10, 0) NOT NULL,
    HR_Contract_ID NUMERIC(10, 0) NOT NULL,
    HR_Department_ID NUMERIC(10, 0) NOT NULL,
    HR_Job_ID NUMERIC(10, 0) NOT NULL,
    HR_Movement_ID NUMERIC(10, 0) NOT NULL,
    HR_Payroll_ID NUMERIC(10, 0) NOT NULL,
    HR_Period_ID NUMERIC(10, 0) NOT NULL,
    HR_Process_ID NUMERIC(10, 0) NOT NULL,
    LVE_HR_ProcessReport_ID NUMERIC(10, 0) NOT NULL,
    MovDescription CHARACTER VARYING(255),
    Name CHARACTER VARYING(255) NOT NULL,
    Name2 CHARACTER VARYING(255) NOT NULL,
    Payroll CHARACTER VARYING(255) NOT NULL,
    PrintName CHARACTER VARYING(255) NOT NULL,
    ProcessReport CHARACTER VARYING(255) NOT NULL,
    ReceiptFooterMsg CHARACTER VARYING(255) NOT NULL,
    SeqNo NUMERIC(10, 0) NOT NULL,
    ServiceDate TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    TextMsg CHARACTER VARYING(255) NOT NULL,
    ValidFrom TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    ValidTo TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    Value CHARACTER VARYING(255) NOT NULL
);

-- Elimina la talba si ya existe
 DROP TABLE IF EXISTS LVE_RV_HR_BANAVIH;
-- Crea una tabla nueva
 CREATE TABLE IF NOT EXISTS LVE_RV_HR_BANAVIH (
    -- Identificacion de la tabla
    -- LVE_RV_HR_BANAVIH_ID NUMERIC(10, 0) PRIMARY KEY,
    -- LVE_RV_HR_BANAVIH_UU CHARACTER VARYING(36) DEFAULT NULL::CHARACTER VARYING UNIQUE,
    -- Estos campos son requistos del sistema
    -- AD_Client_ID NUMERIC(10, 0) NOT NULL,
    -- AD_Org_ID NUMERIC(10, 0) NOT NULL,
    -- IsActive CHARACTER(1) NOT NULL DEFAULT 'Y'::BPCHAR,
    -- Created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    -- CreatedBy NUMERIC(10, 0) NOT NULL,
    -- Updated TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    -- UpdatedBy NUMERIC(10, 0) NOT NULL,
    -- Elementos de la herrameinta
    BP_Nationality CHARACTER(1) NOT NULL DEFAULT 'V'::BPCHAR,
    BP_TaxID NUMERIC(12, 0) NOT NULL,
    BP_Name CHARACTER VARYING(255) NOT NULL,
    BP_Name2 CHARACTER VARYING(255),
    BP_LastName1 CHARACTER VARYING(255) NOT NULL,
    BP_LastName2 CHARACTER VARYING(255),
    Amt NUMERIC(22, 2) NOT NULL,
    StartDate TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    EndDate TIMESTAMP WITHOUT TIME ZONE
    );
