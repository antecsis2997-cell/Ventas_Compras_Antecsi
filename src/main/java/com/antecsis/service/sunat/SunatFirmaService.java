package com.antecsis.service.sunat;

import com.antecsis.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Firma digitalmente un documento XML UBL 2.1 con el certificado PFX del emisor.
 * La firma XMLDSig se inserta dentro del nodo ext:ExtensionContent del documento.
 * Cumple con los requisitos de SUNAT para el SEE del Contribuyente.
 */
@Slf4j
@Service
public class SunatFirmaService {

    private static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";

    /**
     * @param xmlSinFirma  XML UBL 2.1 generado por Freemarker (sin firma).
     * @param pfxBase64    Contenido del archivo .PFX en Base64 (descifrado).
     * @param pfxPassword  Contraseña del .PFX (descifrada).
     * @return XML con la firma digital insertada en ext:ExtensionContent.
     */
    public String firmar(String xmlSinFirma, String pfxBase64, String pfxPassword) {
        try {
            // 1. Cargar el certificado PFX
            byte[] pfxBytes = Base64.getDecoder().decode(pfxBase64);
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(pfxBytes), pfxPassword.toCharArray());
            String alias = obtenerAlias(ks);
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, pfxPassword.toCharArray());
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

            // 2. Parsear el XML como DOM (namespace-aware es obligatorio)
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlSinFirma)));

            // 3. Preparar la factoría de firma XMLDSig
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            // 4. Crear la referencia: firma sobre el documento completo (transform ENVELOPED)
            Reference ref = fac.newReference(
                    "",
                    fac.newDigestMethod(DigestMethod.SHA256, null),
                    Collections.singletonList(
                            fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)
                    ),
                    null, null
            );

            // 5. Crear SignedInfo con C14N y RSA-SHA256
            SignedInfo si = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(
                            CanonicalizationMethod.INCLUSIVE,
                            (C14NMethodParameterSpec) null
                    ),
                    fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                    Collections.singletonList(ref)
            );

            // 6. Crear KeyInfo con el certificado X.509
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509Data));

            // 7. Localizar el nodo ext:ExtensionContent donde irá la firma
            Node extensionContent = doc.getElementsByTagNameNS(NS_EXT, "ExtensionContent").item(0);
            if (extensionContent == null) {
                // Si no existe, crear el bloque UBLExtensions mínimo
                extensionContent = crearExtensionContent(doc);
            }

            // 8. Firmar e insertar en el DOM
            DOMSignContext dsc = new DOMSignContext(privateKey, extensionContent);
            XMLSignature signature = fac.newXMLSignature(si, ki);
            signature.sign(dsc);

            // 9. Serializar de vuelta a String
            return serializarDoc(doc);

        } catch (Exception e) {
            log.error("Error firmando XML SUNAT: {}", e.getMessage(), e);
            throw new BusinessException("Error en firma digital del comprobante: " + e.getMessage());
        }
    }

    private String obtenerAlias(KeyStore ks) throws Exception {
        Enumeration<String> aliases = ks.aliases();
        if (!aliases.hasMoreElements()) {
            throw new BusinessException("El certificado PFX no contiene claves");
        }
        return aliases.nextElement();
    }

    private Node crearExtensionContent(Document doc) {
        Element extensions = doc.createElementNS(NS_EXT, "ext:UBLExtensions");
        Element extension = doc.createElementNS(NS_EXT, "ext:UBLExtension");
        Element content = doc.createElementNS(NS_EXT, "ext:ExtensionContent");
        extension.appendChild(content);
        extensions.appendChild(extension);
        doc.getDocumentElement().insertBefore(extensions, doc.getDocumentElement().getFirstChild());
        return content;
    }

    private String serializarDoc(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}
