<?xml version="1.0" encoding="UTF-8"?><Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
  xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
  xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
  xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
  xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2">
  <ext:UBLExtensions>
    <ext:UBLExtension>
      <ext:ExtensionContent/>
    </ext:UBLExtension>
  </ext:UBLExtensions>
  <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
  <cbc:CustomizationID>2.0</cbc:CustomizationID>
  <cbc:ID>${m.serie}-${m.correlativo?string("00000000")}</cbc:ID>
  <cbc:IssueDate>${m.fechaEmision}</cbc:IssueDate>
  <cbc:IssueTime>${m.horaEmision}</cbc:IssueTime>
  <cbc:InvoiceTypeCode listID="${m.tipoDocumentoListId}">${m.tipoDocumento}</cbc:InvoiceTypeCode>
  <cbc:Note languageLocaleID="1000"><![CDATA[${m.notaMontoLetras}]]></cbc:Note>
  <cbc:DocumentCurrencyCode>${m.moneda}</cbc:DocumentCurrencyCode>
  <cac:Signature>
    <cbc:ID>EMPRESA-SIGN</cbc:ID>
    <cac:SignatoryParty>
      <cac:PartyIdentification>
        <cbc:ID>${m.emisorRuc}</cbc:ID>
      </cac:PartyIdentification>
      <cac:PartyName>
        <cbc:Name><![CDATA[${m.emisorRazonSocial}]]></cbc:Name>
      </cac:PartyName>
    </cac:SignatoryParty>
    <cac:DigitalSignatureAttachment>
      <cac:ExternalReference>
        <cbc:URI>#EMPRESA-SIGN</cbc:URI>
      </cac:ExternalReference>
    </cac:DigitalSignatureAttachment>
  </cac:Signature>
  <cac:AccountingSupplierParty>
    <cac:Party>
      <cac:PartyIdentification>
        <cbc:ID schemeID="6">${m.emisorRuc}</cbc:ID>
      </cac:PartyIdentification>
      <cac:PartyName>
        <cbc:Name><![CDATA[${m.emisorNombreComercial}]]></cbc:Name>
      </cac:PartyName>
      <cac:PartyLegalEntity>
        <cbc:RegistrationName><![CDATA[${m.emisorRazonSocial}]]></cbc:RegistrationName>
        <cac:RegistrationAddress>
          <cbc:ID>${m.emisorUbigeo}</cbc:ID>
          <cbc:AddressTypeCode>0000</cbc:AddressTypeCode>
          <cbc:CitySubdivisionName>--</cbc:CitySubdivisionName>
          <cbc:CityName><![CDATA[${m.emisorProvincia}]]></cbc:CityName>
          <cbc:CountrySubentity><![CDATA[${m.emisorDepartamento}]]></cbc:CountrySubentity>
          <cbc:District><![CDATA[${m.emisorDistrito}]]></cbc:District>
          <cac:Country>
            <cbc:IdentificationCode>PE</cbc:IdentificationCode>
          </cac:Country>
        </cac:RegistrationAddress>
      </cac:PartyLegalEntity>
    </cac:Party>
  </cac:AccountingSupplierParty>
  <cac:AccountingCustomerParty>
    <cac:Party>
      <cac:PartyIdentification>
        <cbc:ID schemeID="${m.receptorTipoDoc}">${m.receptorNumDoc}</cbc:ID>
      </cac:PartyIdentification>
      <cac:PartyLegalEntity>
        <cbc:RegistrationName><![CDATA[${m.receptorNombre}]]></cbc:RegistrationName>
        <#if m.receptorDireccion?has_content>
        <cac:RegistrationAddress>
          <cac:AddressLine>
            <cbc:Line><![CDATA[${m.receptorDireccion}]]></cbc:Line>
          </cac:AddressLine>
        </cac:RegistrationAddress>
        </#if>
      </cac:PartyLegalEntity>
    </cac:Party>
  </cac:AccountingCustomerParty>
  <cac:PaymentTerms>
    <cbc:ID>FormaPago</cbc:ID>
    <cbc:PaymentMeansID>${m.formaPago}</cbc:PaymentMeansID>
  </cac:PaymentTerms>
  <cac:TaxTotal>
    <cbc:TaxAmount currencyID="${m.moneda}">${m.totalIgv?string["0.00"]}</cbc:TaxAmount>
    <cac:TaxSubtotal>
      <cbc:TaxableAmount currencyID="${m.moneda}">${m.totalValorVenta?string["0.00"]}</cbc:TaxableAmount>
      <cbc:TaxAmount currencyID="${m.moneda}">${m.totalIgv?string["0.00"]}</cbc:TaxAmount>
      <cac:TaxCategory>
        <cbc:ID>S</cbc:ID>
        <cbc:Percent>18</cbc:Percent>
        <cbc:TaxExemptionReasonCode>10</cbc:TaxExemptionReasonCode>
        <cac:TaxScheme>
          <cbc:ID>1000</cbc:ID>
          <cbc:Name>IGV</cbc:Name>
          <cbc:TaxTypeCode>VAT</cbc:TaxTypeCode>
        </cac:TaxScheme>
      </cac:TaxCategory>
    </cac:TaxSubtotal>
  </cac:TaxTotal>
  <cac:LegalMonetaryTotal>
    <cbc:LineExtensionAmount currencyID="${m.moneda}">${m.totalValorVenta?string["0.00"]}</cbc:LineExtensionAmount>
    <cbc:TaxInclusiveAmount currencyID="${m.moneda}">${m.totalPagar?string["0.00"]}</cbc:TaxInclusiveAmount>
    <cbc:PayableAmount currencyID="${m.moneda}">${m.totalPagar?string["0.00"]}</cbc:PayableAmount>
  </cac:LegalMonetaryTotal>
  <#list m.lineas as linea>
  <cac:InvoiceLine>
    <cbc:ID>${linea.numero?c}</cbc:ID>
    <cbc:InvoicedQuantity unitCode="${linea.unidadMedida}">${linea.cantidad?string["0.##"]}</cbc:InvoicedQuantity>
    <cbc:LineExtensionAmount currencyID="${m.moneda}">${linea.valorVentaLinea?string["0.00"]}</cbc:LineExtensionAmount>
    <cac:PricingReference>
      <cac:AlternativeConditionPrice>
        <cbc:PriceAmount currencyID="${m.moneda}">${linea.precioConIgv?string["0.00"]}</cbc:PriceAmount>
        <cbc:PriceTypeCode>01</cbc:PriceTypeCode>
      </cac:AlternativeConditionPrice>
    </cac:PricingReference>
    <cac:TaxTotal>
      <cbc:TaxAmount currencyID="${m.moneda}">${linea.igvLinea?string["0.00"]}</cbc:TaxAmount>
      <cac:TaxSubtotal>
        <cbc:TaxableAmount currencyID="${m.moneda}">${linea.valorVentaLinea?string["0.00"]}</cbc:TaxableAmount>
        <cbc:TaxAmount currencyID="${m.moneda}">${linea.igvLinea?string["0.00"]}</cbc:TaxAmount>
        <cac:TaxCategory>
          <cbc:ID>S</cbc:ID>
          <cbc:Percent>18</cbc:Percent>
          <cbc:TaxExemptionReasonCode>${linea.codigoAfectacionIgv}</cbc:TaxExemptionReasonCode>
          <cac:TaxScheme>
            <cbc:ID>1000</cbc:ID>
            <cbc:Name>IGV</cbc:Name>
            <cbc:TaxTypeCode>VAT</cbc:TaxTypeCode>
          </cac:TaxScheme>
        </cac:TaxCategory>
      </cac:TaxSubtotal>
    </cac:TaxTotal>
    <cac:Item>
      <cbc:Description><![CDATA[${linea.descripcion}]]></cbc:Description>
      <cac:SellersItemIdentification>
        <cbc:ID>${linea.codigoProducto}</cbc:ID>
      </cac:SellersItemIdentification>
    </cac:Item>
    <cac:Price>
      <cbc:PriceAmount currencyID="${m.moneda}">${linea.valorUnitarioSinIgv?string["0.00"]}</cbc:PriceAmount>
    </cac:Price>
  </cac:InvoiceLine>
  </#list>
</Invoice>
