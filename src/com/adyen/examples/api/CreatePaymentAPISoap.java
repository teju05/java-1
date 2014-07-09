package com.adyen.examples.api;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import com.adyen.services.common.Address;
import com.adyen.services.common.Amount;
import com.adyen.services.payment.Card;
import com.adyen.services.payment.Payment;
import com.adyen.services.payment.PaymentPortType;
import com.adyen.services.payment.PaymentRequest;
import com.adyen.services.payment.PaymentResult;
import com.adyen.services.payment.ServiceException;

/**
 * Create Payment through the API (SOAP)
 * 
 * Payments can be created through our API, however this is only possible if you are PCI Compliant. SOAP API payments
 * are submitted using the authorise action. We will explain a simple credit card submission.
 * 
 * Please note: using our API requires a web service user. Set up your Webservice user:
 * Adyen CA >> Settings >> Users >> ws@Company. >> Generate Password >> Submit
 * 
 * @link /2.API/Soap/CreatePaymentAPI
 * @author Created by Adyen - Payments Made Easy
 */

@WebServlet(urlPatterns = { "/2.API/Soap/CreatePaymentAPI" })
public class CreatePaymentAPISoap extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/**
		 * SOAP settings
		 * - wsdl: the WSDL url you are using (Test/Live)
		 * - wsUser: your web service user
		 * - wsPassword: your web service user's password
		 */
		String wsdl = "https://pal-test.adyen.com/pal/Payment.wsdl";
		String wsUser = "YourWSUser";
		String wsPassword = "YourWSPassword";

		/**
		 * Create SOAP client, using classes in adyen-wsdl-cxf.jar library (generated by wsdl2java tool, Apache CXF).
		 * 
		 * @see WebContent/WEB-INF/lib/adyen-wsdl-cxf.jar
		 */
		Payment service = new Payment(new URL(wsdl));
		PaymentPortType client = service.getPaymentHttpPort();

		// Set HTTP Authentication
		((BindingProvider) client).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, wsUser);
		((BindingProvider) client).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, wsPassword);

		/**
		 * A payment can be submitted by sending a PaymentRequest to the authorise action of the web service.
		 * The request should contain the following variables:
		 * 
		 * <pre>
		 * - merchantAccount: the merchant account the payment was processed with.
		 * - amount: the amount of the payment
		 *     - currency: the currency of the payment
		 *     - amount: the amount of the payment
		 * - reference: your reference
		 * - shopperIP: the IP address of the shopper (recommended)
		 * - shopperEmail: the e-mail address of the shopper 
		 * - shopperReference: the shopper reference, i.e. the shopper ID
		 * - fraudOffset: numeric value that will be added to the fraud score (optional)
		 * - card
		 *     - billingAddress: we advice you to submit billingAddress data if available for risk checks;
		 *         - street: the street name
		 *         - postalCode: the postal/zip code
		 *         - city: the city
		 *         - houseNumberOrName: the house number/name
		 *         - stateOrProvince: the state or province
		 *         - country: the country
		 *     - expiryMonth: the expiration month of the card, written as a 2-digit string, padded with 0 if required
		 *                    (e.g. 03 or 12)
		 *     - expiryYear: the expiration year of the card, full-written (e.g. 2016)
		 *     - holderName: the card holder's name, as embossed on the card
		 *     - number: the card number
		 *     - cvc: the card validation code, which is the CVC2 (MasterCard), CVV2 (Visa) or CID (American Express)
		 * </pre>
		 */

		// Create new payment request
		PaymentRequest paymentRequest = new PaymentRequest();
		paymentRequest.setMerchantAccount("YourMerchantAccount");
		paymentRequest.setReference("TEST-PAYMENT-" + new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()));
		paymentRequest.setShopperIP("1.1.1.1");
		paymentRequest.setShopperEmail("test@example.com");
		paymentRequest.setFraudOffset(0);

		// Set amount
		Amount amount = new Amount();
		amount.setCurrency("EUR");
		amount.setValue(199L);
		paymentRequest.setAmount(amount);

		// Set card
		Card card = new Card();

		Address billingAddress = new Address();
		billingAddress.setStreet("Simon Carmiggeltstraat");
		billingAddress.setPostalCode("1011 DJ");
		billingAddress.setCity("Amsterdam");
		billingAddress.setHouseNumberOrName("6-50");
		billingAddress.setStateOrProvince("");
		billingAddress.setCountry("NL");

		card.setBillingAddress(billingAddress);
		card.setExpiryMonth("06");
		card.setExpiryYear("2016");
		card.setHolderName("John Doe");
		card.setNumber("5555444433331111");
		card.setCvc("737");

		paymentRequest.setCard(card);

		/**
		 * Send the authorise request.
		 * 
		 * If the payment passes validation a risk analysis will be done and, depending on the outcome, an authorisation
		 * will be attempted. You receive a payment response with the following fields:
		 * - pspReference: The reference we assigned to the payment;
		 * - resultCode: The result of the payment. One of Authorised, Refused or Error;
		 * - authCode: An authorisation code if the payment was successful, or blank otherwise;
		 * - refusalReason: If the payment was refused, the refusal reason.
		 */
		PaymentResult result;
		try {
			result = client.authorise(paymentRequest);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}

		// Set payment result in request data and forward it to corresponding JSP page
		request.setAttribute("paymentResult", result);
		request.getRequestDispatcher("/2.API/soap/create-payment-api.jsp").forward(request, response);

	}

	// Forward GET request to corresponding JSP page
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("/2.API/soap/create-payment-api.jsp").forward(request, response);
	}

}
