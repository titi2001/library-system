var app = angular.module('HomeApp',[]);

app.controller('GetBooksController', ['$scope','HomeService', function ($scope, HomeService) {
    $scope.books = [];
    $scope.bookContent = null;
    $scope.book;
    $scope.bookId = "";
    $scope.chapter;
    $scope.user = "";
    $scope.quoteText = "";
    $scope.searchResults = [];
    $scope.bookList;

    $scope.searchForm = {
                title: "",
                authorName: "",
                genre: ""
            };



    $scope.getUser = function () {
            HomeService.getUser()
              .then(function success(response){
                    $scope.user = response.data;
                    $("#shareActivity").prop("checked", $scope.user.shareActivity);
              },
              function error (response){
                  console.log(response.data);
              });
        };
    $scope.getBooks = function () {
     $(".readIcon").hide();
     $("#backButton").hide();
        HomeService.getBooks()
          .then(function success(response){
              $scope.books = response.data;
          },
          function error (response){
              console.log("Error getting books!");
          });
           $scope.getUser();
    };
    $scope.searchBooks = function(){
        var data = new FormData();
        if($scope.searchForm.title.length == 0){data.append("title", "!!!");}
        else{
        data.append("title", $scope.searchForm.title);}
        if($scope.searchForm.authorName.length == 0){data.append("authorName", "!!!");}
        else{data.append("authorName", $scope.searchForm.authorName);}
        if($scope.searchForm.genre.length == 0){data.append("genre", "!!!");}
        else{data.append("genre", $scope.searchForm.genre);}
        console.log($scope.searchForm);
        console.log("test");
        HomeService.searchBooks(data.get("title"), data.get("authorName"), data.get("genre"))
                      .then(function success(response){
                          $scope.searchResults = response.data;
                          $("#bookDisplay").hide();
                          $("#searchResults").show();
                          $("#backButton").show();
                      },
                      function error (response){
                          console.log(response);
                      });
    };
    $scope.getBook = function (id) {
            HomeService.getBook(id)
              .then(function success(response){
                  $scope.book = response.data;
              },
              function error (response){
                  console.log(response);
              });
        };
    $scope.readMode = function (id, chapter) {
        $scope.getUser();
        if(chapter <= 1){
        $("#prevButton").hide()
        HomeService.startBook(id);}
        $(".readIcon").show();
        $("#backButton").show();
        if($scope.book = null){
        $scope.getBook(id);}
        $scope.bookId = id;
        $scope.chapter = chapter;
        if($scope.bookContent == null){
            HomeService.getText(id)
                      .then(function success(response){
                          $scope.bookContent = response.data;
                          $("#bookDisplay").hide();
                          $("#searchResults").hide();
                          $("#profile").hide();
                          $("#bookListHolder").hide();
                          $("#prevButton").hide();
                          $("#textHolder").show();
                          document.getElementById("theContainer").innerHTML = $scope.bookContent[chapter];
                      },
                      function error (response){
                      console.log(response);
                      });
        }
        document.getElementById("theContainer").innerHTML = $scope.bookContent[chapter];
        if(chapter <= 0){
        $("#prevButton").hide()
        }
        else{
            $("#prevButton").show()
        }
        if(chapter >= $scope.bookContent.length-1){
            $("#nextButton").hide();
            HomeService.finishBook(id);
        }
        else{
            $("#nextButton").show();
        }
        };
        $scope.goBack = function(){
             $scope.bookContent = null;
             $scope.bookId = null;
             $scope.chapter = null;
             $("#bookDisplay").show();
             $("#profile").hide();
             $("#backButton").hide();
             $("#bookListHolder").hide();
             $("#searchResults").hide();
             $("#textHolder").hide();
             $(".readIcon").hide();
        };
        $scope.getSelectedText = function(){
            $scope.quoteText = window.getSelection().toString();
        };
        $scope.addQuote = function(){

        if ($scope.quoteText.length > 0) {
            var quote = {
                content: $scope.quoteText,
                userId: $scope.user.id,
                bookId: $scope.bookId
                            };
            HomeService.addQuote(quote)
                   .then(function success(response){
                     console.log("Success!")
                   },
                   function error (response){
                       console.log("Error creating quote.");
                   });}
        };

        $scope.searchInWikipedia = function(){
                window.location.replace("/book/wikipedia/" + $scope.quoteText);};

    $scope.addBookList = function(){
                var bookList = {
                    title: $scope.bookListForm.title,
                    userId: $scope.user.id,
                    bookId: $scope.bookId
                  };
                HomeService.addBookList(bookList)
                       .then(function success(response){
                         console.log("Success!")
                       },
                       function error (response){
                           console.log("Error creating book List.");
                       });};

    $scope.addBookToBookList = function(bookListId, bookId){
                    HomeService.addBookToBookList(bookListId, bookId)
                           .then(function success(response){
                             console.log("Success!")
                           },
                           function error (response){
                               console.log("Error creating book List.");
                           });};
    $scope.goToProfile = function () {
            $scope.getUser();
            $("#bookDisplay").hide();
             $(".readIcon").hide();
             $("#bookListHolder").hide();
            $("#textHolder").hide();
            $("#profile").show();
            $("#backButton").show();
            };
    $scope.openBookList = function (id) {
                 HomeService.getBookList(id)
                               .then(function success(response){
                                   $scope.bookList = response.data;
                                    $("#profile").hide();
                                    $("#backButton").show();
                                    $("#bookListHolder").show();
                               },
                               function error (response){
                                   console.log(response);
                               });
                };
    $scope.downloadBook = function(id){
        window.open("http://localhost:8007/book/download/" + id, '_blank');
    };
    $scope.toggleShareActivity = function(){
            HomeService.toggleShareActivity()
                                   .then(function success(response){
                                   $scope.getUser();

                                     console.log("Success!")
                                   },
                                   function error (response){
                                       console.log("Error.");
                                   });
        };

}]);

app.controller('TwitterLoginController', ['$scope','HomeService', function ($scope,HomeService) {
    $scope.user;
    $scope.getUser = function () {
            HomeService.getUser()
              .then(function success(response){
                console.log(response.data);
                  if(response.data == ""){
                    $("#loggedIn").remove();
                  }
                  else{
                    $scope.user = response.data;
                    $("#notLogged").remove();
                  }
              },
              function error (response){
                  console.log("Error getting user!");
              });
        }

}]);

app.service('HomeService',['$http', function ($http) {


    this.getUser = function getUser(){
        return $http({
                  method: 'GET',
                  url: '/getUser'
                });
    }

    this.getBook = function getBook(id){
            return $http({
              method: 'GET',
              url: '/book/get/' + id
            });
        }

    this.getBooks = function getBooks(){
        return $http({
          method: 'GET',
          url: '/book/getAll'
        });
    }
    this.searchBooks = function searchBooks(title, authorName, genre){
            console.log(title);
            return $http({
              method: 'GET',
              url: '/book/search/' + title + '/' + authorName + '/' + genre
            });
        }
    this.getBookLists = function getBookLists(id){
            return $http({
              method: 'GET',
              url: 'bookList/getBookListsOfUser/' + id
            });
        }
    this.getText = function getText(id){
            return $http({
              method: 'GET',
              url: '/book/getText/' + id
            });
        }
    this.getBookList = function getBookList(id){
                return $http({
                  method: 'GET',
                  url: '/bookList/get/' + id
                });
            }
    this.addQuote = function addQuote(quote){
                return $http({
                          method: 'POST',
                          url: '/quote/add',
                          contentType:'application/json',
                          dataType:'json',
                          data: quote
                       });
            }
    this.addBookList = function addBookList(bookList){
                    return $http({
                              method: 'POST',
                              url: '/bookList/add',
                              contentType:'application/json',
                              dataType:'json',
                              data: bookList
                           });
                }
    this.addBookToBookList = function addBookToBookList(bookListId, bookId){
                        return $http({
                                  method: 'PUT',
                                  url: '/bookList/addBookToBookList/' + bookListId + '/' + bookId
                               });
                    }
    this.startBook = function startBook(id){
                        return $http({
                                  method: 'PUT',
                                  url: 'startBook/' + id
                               });
                    }
    this.finishBook = function finishBook(id){
                            return $http({
                                      method: 'PUT',
                                      url: 'finishBook/' + id
                                   });
                        }
    this.toggleShareActivity = function toggleShareActivity(){
                            return $http({
                                      method: 'PUT',
                                      url: 'toggleShareActivity'
                                   });
                        }
}]);


function getAuthorsAsString(authors){
    var result = "";
    for(var i = 0; i < authors.length; i++){
        result.concat(authors[i].name);
    }
    return result;
    }

    $(document).ready(function() {
     $("#textHolder").hide();
     $(".readIcon").hide();
     $("#backButton").hide();
     $("#profile").hide();
     $("#bookListHolder").hide();
      $(window).on('load scroll resize', function() {

        var docHeight = $(document).height();
        var windowPos = $(window).scrollTop();
        var windowHeight = $(window).height();
        var windowWidth = $(window).width();
        var completion = windowPos / (docHeight - windowHeight);

        if (docHeight <= windowHeight) {
          $('#progress').width(windowWidth);
        } else {
          $('#progress').width(completion * windowWidth);
        }

      });
    });