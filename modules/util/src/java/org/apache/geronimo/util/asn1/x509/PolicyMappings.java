/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.geronimo.util.asn1.ASN1Sequence;
import org.apache.geronimo.util.asn1.ASN1Encodable;
import org.apache.geronimo.util.asn1.ASN1EncodableVector;
import org.apache.geronimo.util.asn1.DERObject;
import org.apache.geronimo.util.asn1.DERObjectIdentifier;
import org.apache.geronimo.util.asn1.DERSequence;

/**
 * PolicyMappings V3 extension, described in RFC3280.
 * <pre>
 *    PolicyMappings ::= SEQUENCE SIZE (1..MAX) OF SEQUENCE {
 *      issuerDomainPolicy      CertPolicyId,
 *      subjectDomainPolicy     CertPolicyId }
 * </pre>
 *
 * @see <a href="http://www.faqs.org/rfc/rfc3280.txt">RFC 3280, section 4.2.1.6</a>
 */
public class PolicyMappings
    extends ASN1Encodable
{
   ASN1Sequence seq = null;

   /**
    * Creates a new <code>PolicyMappings</code> instance.
    *
    * @param seq an <code>ASN1Sequence</code> constructed as specified
    * in RFC 3280
    */
   public PolicyMappings (ASN1Sequence seq)
      {
         this.seq = seq;
      }

   /**
    * Creates a new <code>PolicyMappings</code> instance.
    *
    * @param mappings a <code>HashMap</code> value that maps
    * <code>String</code> oids
    * to other <code>String</code> oids.
    */
   public PolicyMappings (Hashtable mappings)
      {
         ASN1EncodableVector dev = new ASN1EncodableVector();
         Enumeration it = mappings.keys();

         while (it.hasMoreElements()) {
            String idp = (String) it.nextElement();
            String sdp = (String) mappings.get(idp);
            ASN1EncodableVector dv = new ASN1EncodableVector();
            dv.add(new DERObjectIdentifier(idp));
            dv.add(new DERObjectIdentifier(sdp));
            dev.add(new DERSequence(dv));
         }

         seq = new DERSequence(dev);
      }

   public DERObject toASN1Object()
      {
         return seq;
      }
}