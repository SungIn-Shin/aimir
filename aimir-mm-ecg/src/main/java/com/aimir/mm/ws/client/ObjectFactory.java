
package com.aimir.mm.ws.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the com.aimir.cms.ws.client package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _CustBalanceReq_QNAME = new QName("http://server.ws.mm.aimir.com/", "CustBalanceReq");
	private final static QName _CustBalanceResp_QNAME = new QName("http://server.ws.mm.aimir.com/", "CustBalanceResp");
	private final static QName _VendBalanceReq_QNAME = new QName("http://server.ws.mm.aimir.com/", "VendBalanceReq");
	private final static QName _VendBalanceResp_QNAME = new QName("http://server.ws.mm.aimir.com/", "VendBalanceResp");
	private final static QName _CustRechargeReq_QNAME = new QName("http://server.ws.mm.aimir.com/", "CustRechargeReq");
	private final static QName _CustRechargeResp_QNAME = new QName("http://server.ws.mm.aimir.com/", "CustRechargeResp");
	private final static QName _VendRechargeReq_QNAME = new QName("http://server.ws.mm.aimir.com/", "VendRechargeReq");
	private final static QName _VendRechargeResp_QNAME = new QName("http://server.ws.mm.aimir.com/", "VendRechargeResp");
	private final static QName _PmtVerificationReq_QNAME = new QName("http://server.ws.mm.aimir.com/", "PmtVerificationReq");
	private final static QName _PmtVerificationResp_QNAME = new QName("http://server.ws.mm.aimir.com/", "PmtVerificationResp");
	private final static QName _CustLastPurReq_QNAME = new QName("http://server.ws.mm.aimir.com/", "CustLastPurReq");
	private final static QName _CustLastPurResp_QNAME = new QName("http://server.ws.mm.aimir.com/", "CustLastPurResp");
	private final static QName _ConHistoryReq_QNAME = new QName("http://server.ws.mm.aimir.com/", "ConHistoryReq");
	private final static QName _ConHistoryResp_QNAME = new QName("http://server.ws.mm.aimir.com/", "ConHistoryResp");
	private final static QName _BillHistoryReq_QNAME = new QName("http://server.ws.mm.aimir.com/", "BillHistoryReq");
	private final static QName _BillHistoryResp_QNAME = new QName("http://server.ws.mm.aimir.com/", "BillHistoryResp");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: com.aimir.mm.ws.client
	 * 
	 */
	public ObjectFactory() {
	}

	public CustBalanceReq createCustBalanceReq() {
		return new CustBalanceReq();
	}

	public CustBalanceResp createCustBalanceResp() {
		return new CustBalanceResp();
	}

	public VendBalanceReq createVendBalanceReq() {
		return new VendBalanceReq();
	}

	public VendBalanceResp createVendBalanceResp() {
		return new VendBalanceResp();
	}

	public CustRechargeReq createCustRechargeReq() {
		return new CustRechargeReq();
	}

	public CustRechargeResp createCustRechargeResp() {
		return new CustRechargeResp();
	}

	public VendRechargeReq createVendRechargeReq() {
		return new VendRechargeReq();
	}

	public VendRechargeResp createVendRechargeResp() {
		return new VendRechargeResp();
	}

	public PmtVerificationReq createPmtVerificationReq() {
		return new PmtVerificationReq();
	}

	public PmtVerificationResp createPmtVerificationResp() {
		return new PmtVerificationResp();
	}

	public CustLastPurReq createCustLastPurReq() {
		return new CustLastPurReq();
	}

	public CustLastPurResp createCustLastPurResp() {
		return new CustLastPurResp();
	}

	public ConHistoryReq createConHistoryReq() {
		return new ConHistoryReq();
	}

	public ConHistoryResp createConHistoryResp() {
		return new ConHistoryResp();
	}

	public BillHistoryReq createBillHistoryReq() {
		return new BillHistoryReq();
	}

	public BillHistoryResp createBillHistoryResp() {
		return new BillHistoryResp();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link -Req }{@code >}
	 * } Create an instance of {@link JAXBElement }{@code <}{@link -Resp }
	 * {@code >}}
	 * 
	 */
	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "CustBalanceReq")
	public JAXBElement<CustBalanceReq> createCustBalanceReq(CustBalanceReq value) {
		return new JAXBElement<CustBalanceReq>(_CustBalanceReq_QNAME, CustBalanceReq.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "CustBalanceResp")
	public JAXBElement<CustBalanceResp> createCustBalanceResp(CustBalanceResp value) {
		return new JAXBElement<CustBalanceResp>(_CustBalanceResp_QNAME, CustBalanceResp.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "VendBalanceReq")
	public JAXBElement<VendBalanceReq> createVendBalanceReq(VendBalanceReq value) {
		return new JAXBElement<VendBalanceReq>(_VendBalanceReq_QNAME, VendBalanceReq.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "VendBalanceResp")
	public JAXBElement<VendBalanceResp> createVendBalanceResp(VendBalanceResp value) {
		return new JAXBElement<VendBalanceResp>(_VendBalanceResp_QNAME, VendBalanceResp.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "CustRechargeReq")
	public JAXBElement<CustRechargeReq> createCustRechargeReq(CustRechargeReq value) {
		return new JAXBElement<CustRechargeReq>(_CustRechargeReq_QNAME, CustRechargeReq.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "CustRechargeResp")
	public JAXBElement<CustRechargeResp> createCustRechargeResp(CustRechargeResp value) {
		return new JAXBElement<CustRechargeResp>(_CustRechargeResp_QNAME, CustRechargeResp.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "VendRechargeReq")
	public JAXBElement<VendRechargeReq> createVendRechargeReq(VendRechargeReq value) {
		return new JAXBElement<VendRechargeReq>(_VendRechargeReq_QNAME, VendRechargeReq.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "VendRechargeResp")
	public JAXBElement<VendRechargeResp> createVendRechargeResp(VendRechargeResp value) {
		return new JAXBElement<VendRechargeResp>(_VendRechargeResp_QNAME, VendRechargeResp.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "PmtVerificationReq")
	public JAXBElement<PmtVerificationReq> createPmtVerificationReq(PmtVerificationReq value) {
		return new JAXBElement<PmtVerificationReq>(_PmtVerificationReq_QNAME, PmtVerificationReq.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "PmtVerificationResp")
	public JAXBElement<PmtVerificationResp> createPmtVerificationResp(PmtVerificationResp value) {
		return new JAXBElement<PmtVerificationResp>(_PmtVerificationResp_QNAME, PmtVerificationResp.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "CustLastPurReq")
	public JAXBElement<CustLastPurReq> createCustLastPurReq(CustLastPurReq value) {
		return new JAXBElement<CustLastPurReq>(_CustLastPurReq_QNAME, CustLastPurReq.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "CustLastPurResp")
	public JAXBElement<CustLastPurResp> createCustLastPurResp(CustLastPurResp value) {
		return new JAXBElement<CustLastPurResp>(_CustLastPurResp_QNAME, CustLastPurResp.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "ConHistoryReq")
	public JAXBElement<ConHistoryReq> createConHistoryReq(ConHistoryReq value) {
		return new JAXBElement<ConHistoryReq>(_ConHistoryReq_QNAME, ConHistoryReq.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "ConHistoryResp")
	public JAXBElement<ConHistoryResp> createConHistoryResp(ConHistoryResp value) {
		return new JAXBElement<ConHistoryResp>(_ConHistoryResp_QNAME, ConHistoryResp.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "BillHistoryReq")
	public JAXBElement<BillHistoryReq> createBillHistoryReq(BillHistoryReq value) {
		return new JAXBElement<BillHistoryReq>(_BillHistoryReq_QNAME, BillHistoryReq.class, null, value);
	}

	@XmlElementDecl(namespace = "http://server.ws.mm.aimir.com/", name = "BillHistoryResp")
	public JAXBElement<BillHistoryResp> createBillHistoryResp(BillHistoryResp value) {
		return new JAXBElement<BillHistoryResp>(_BillHistoryResp_QNAME, BillHistoryResp.class, null, value);
	}
}
