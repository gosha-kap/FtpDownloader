$("#everyDay").click(function(){
    if ($(this).is(':checked')){
        $('#localTime').removeAttr('disabled');
    } else {
        $('#localTime').attr('disabled', 'disabled');
        $('#localTime').val('');
    }
});

$("#once").click(function(){
    if ($(this).is(':checked')){
        $('#dateTime').removeAttr('disabled');
    } else {
        $('#dateTime').attr('disabled', 'disabled');
        $('#dateTime').val('');
    }
});

if($('#localTime').val().length == 0){
    $("#everyDay").attr('checked',false);
}
else{
    $("#everyDay").attr('checked',true);
    $('#localTime').removeAttr('disabled');
}

if($('#dateTime').val().length == 0){
    $("#once").attr('checked',false);
}
else{
    $("#once").attr('checked',true);
    $('#dateTime').removeAttr('disabled');
}