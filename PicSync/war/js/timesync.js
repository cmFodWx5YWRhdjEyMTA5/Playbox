var qrcode = new QRCode(document.getElementById("qrcode"), {
    text: "not-initialised",
    width: 240,
    height: 240,
    colorDark : "#000000",
    colorLight : "#ffffff",
    correctLevel : QRCode.CorrectLevel.H
});

function makeCode (content) {
	qrcode.makeCode(content); // make another code.
}