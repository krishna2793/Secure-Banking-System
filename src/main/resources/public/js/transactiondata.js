var cast = {
    "transactions":[
        {
            "ID":"127466333",
            "Transaction amount":"$1000"
        },
        {
            "ID":"127464555",
            "Transaction amount":"$1020"
        },
        {
            "ID":"1274624565",
            "Transaction amount":"$1200"
        },
        {
            "ID":"127462870",
            "Transaction amount":"$1700"
        },
        {
            "ID":"127462559",
            "Transaction amount":"$1800"
        },
        {
            "ID":"127462554",
            "Transaction amount":"$1900"
        }
    ]
}
$(document).ready(function(){
    var characterTemplate=$("#userlist").html();
    var compiledcharacterTemplate= Handlebars.compile(characterTemplate);
    $(".userlist").html(compiledcharacterTemplate(cast));
});