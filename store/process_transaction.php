<?php
	// import the PHP library
	require __DIR__ . '/lib/autoload.php';
	use Braintree\Gateway;

	$gateway = new Gateway([
		'environment' => 'sandbox',
		'merchantId' => 'MERCHANT-ID',
		'publicKey' => 'PUBLIC-KEY',
		'privateKey' => 'PRIVATE-KEY'
	]);
	
	$amount = $_POST["amount"];
	$currency = $_POST["currency_iso_code"];
	$nonceFromTheClient = $_POST["payment_method_nonce"];
	$deviceDataFromTheClient = $_POST["client_device_data"];
	
	// Define a separate case in the switch for each additional currency your store supports
	switch ($currency) {
		case "USD":
			$merchantAccount = "currency_usd";
			break;
		case "EUR":
			$merchantAccount = "currency_eur";
			break;
		default:
			$merchantAccount = "DEFAULT-MERCHANT-ID";
	}
	
	$result = $gateway->transaction()->sale([
	    'amount' => $amount,
	    'paymentMethodNonce' => $nonceFromTheClient,
	    'deviceData' => $deviceDataFromTheClient,
		'merchantAccountId' => $merchantAccount,
	    'options' => [
			'submitForSettlement' => True
	    ]
	]);
	
	if ($result->success) {
	  echo ($outcome = "SUCCESSFUL");
	} else {
	  echo ($outcome = "UNSUCCESSFUL");
	}
	
?>