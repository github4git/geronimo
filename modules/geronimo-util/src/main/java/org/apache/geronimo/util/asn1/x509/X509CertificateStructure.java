/**
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.util.asn1.x509;

import org.apache.geronimo.util.asn1.ASN1Encodable;
import org.apache.geronimo.util.asn1.ASN1Sequence;
import org.apache.geronimo.util.asn1.ASN1TaggedObject;
import org.apache.geronimo.util.asn1.DERBitString;
import org.apache.geronimo.util.asn1.DERInteger;
import org.apache.geronimo.util.asn1.DERObject;
import org.apache.geronimo.util.asn1.pkcs.PKCSObjectIdentifiers;

/**
 * an X509Certificate structure.
 * <pre>
 *  Certificate ::= SEQUENCE {
 *      tbsCertificate          TBSCertificate,
 *      signatureAlgorithm      AlgorithmIdentifier,
 *      signature               BIT STRING
 *  }
 * </pre>
 */
public class X509CertificateStructure
    extends ASN1Encodable
    implements X509ObjectIdentifiers, PKCSObjectIdentifiers
{
    ASN1Sequence  seq;
    TBSCertificateStructure tbsCert;
    AlgorithmIdentifier     sigAlgId;
    DERBitString            sig;

    public static X509CertificateStructure getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static X509CertificateStructure getInstance(
        Object  obj)
    {
        if (obj instanceof X509CertificateStructure)
        {
            return (X509CertificateStructure)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new X509CertificateStructure((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory");
    }

    public X509CertificateStructure(
        ASN1Sequence  seq)
    {
        this.seq = seq;

        //
        // correct x509 certficate
        //
        if (seq.size() == 3)
        {
            tbsCert = TBSCertificateStructure.getInstance(seq.getObjectAt(0));
            sigAlgId = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));

            sig = (DERBitString)seq.getObjectAt(2);
        }
        else
        {
            throw new IllegalArgumentException("sequence wrong size for a certificate");
        }
    }

    public TBSCertificateStructure getTBSCertificate()
    {
        return tbsCert;
    }

    public int getVersion()
    {
        return tbsCert.getVersion();
    }

    public DERInteger getSerialNumber()
    {
        return tbsCert.getSerialNumber();
    }

    public X509Name getIssuer()
    {
        return tbsCert.getIssuer();
    }

    public Time getStartDate()
    {
        return tbsCert.getStartDate();
    }

    public Time getEndDate()
    {
        return tbsCert.getEndDate();
    }

    public X509Name getSubject()
    {
        return tbsCert.getSubject();
    }

    public SubjectPublicKeyInfo getSubjectPublicKeyInfo()
    {
        return tbsCert.getSubjectPublicKeyInfo();
    }

    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return sigAlgId;
    }

    public DERBitString getSignature()
    {
        return sig;
    }

    public DERObject toASN1Object()
    {
        return seq;
    }
}
