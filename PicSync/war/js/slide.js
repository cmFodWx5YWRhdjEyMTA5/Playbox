$(document).ready(function() {

	var $marginLefty = $('#slidemarginleft div.inner');
	$marginLefty.css({
		marginLeft: $marginLefty.outerWidth() + 'px',
		display: 'block'
	});

	$('#slidemarginleft button').click(function() {
		$marginLefty.animate({
			marginLeft: parseInt($marginLefty.css('marginLeft'), 10) == 0 ? $marginLefty.outerWidth() : 0
		});
	});
});  