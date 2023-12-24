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
	
	// pass $clientToken to your front-end
	$clientToken = $gateway->clientToken()->generate();
	
	echo($clientToken = $gateway->clientToken()->generate());
?>