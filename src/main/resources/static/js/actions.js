$("#repeatLater").click(function () {
    if ($(this).is(':checked')) {
        $('#nextTimeRun').removeAttr('disabled');
        $('#numOfRepeats').removeAttr('disabled');

    } else {
        $('#nextTimeRun').attr('disabled', 'disabled');
        $('#numOfRepeats').attr('disabled', 'disabled');
    }
});


if ($("#repeatLater").prop("checked")) {
    $('#nextTimeRun').removeAttr('disabled');
    $('#numOfRepeats').removeAttr('disabled');
}


$("#ftp").click(function () {
    if ($(this).is(':checked')) {
        $('#dataTimeOut').removeAttr('disabled');
        $('#filePostfix').removeAttr('disabled');
        $('#channel').attr('disabled', 'disabled').val('');
        $('#from').attr('disabled', 'disabled').val('');
        $('#to').attr('disabled', 'disabled').val('');
    }
});

$("#hiwatch").click(function () {
    if ($(this).is(':checked')) {
        $('#channel').removeAttr('disabled');
        if($("#onceChoice").prop("checked")){
            $('#from').removeAttr('disabled');
            $('#to').removeAttr('disabled');
        }
        $('#dataTimeOut').attr('disabled', 'disabled').val('');
        $('#filePostfix').attr('disabled', 'disabled').val('');

    }
});

if ($("#ftp").prop("checked")) {
    $('#dataTimeOut').removeAttr('disabled');
    $('#filePostfix').removeAttr('disabled');
}

if ($("#hiwatch").prop("checked")) {
    $('#channel').removeAttr('disabled');
    $('#from').removeAttr('disabled');
    $('#to').removeAttr('disabled');
}


if ($("#onceChoice").prop("checked")) {
    $('#once').removeAttr('disabled');
    if ($("#hiwatch").prop("checked")) {
        $('#from').removeAttr('disabled');
        $('#to').removeAttr('disabled');
    }
}

if ($("#regularChoice").prop("checked")) {
    $('#regular').removeAttr('disabled');
    if ($("#hiwatch").prop("checked")) {
        $('#from').attr('disabled', 'disabled');
        $('#to').attr('disabled', 'disabled');
    }
}

$("#onceChoice").click(function () {
    if ($(this).is(':checked')) {
        $('#once').removeAttr('disabled');
        $('#regular').attr('disabled', 'disabled').val('');
        if ($("#hiwatch").prop("checked")) {
            $('#from').removeAttr('disabled');
            $('#to').removeAttr('disabled');
        }
    }

});

$("#regularChoice").click(function () {
    if ($(this).is(':checked')) {
        $('#regular').removeAttr('disabled');
        $('#once').attr('disabled', 'disabled').val('');
        if ($("#hiwatch").prop("checked")) {
            $('#from').attr('disabled', 'disabled').val('');
            $('#to').attr('disabled', 'disabled').val('');
        }
    }

});



