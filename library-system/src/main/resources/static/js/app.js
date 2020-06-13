var app = angular.module('HomeApp', ['ui.router', 'ngCookies']);

//App Configuration

app.directive('fileModel', ['$parse', function($parse) {
 return {
  restrict: 'A',
  link: function(scope, element, attrs) {
   var model = $parse(attrs.fileModel);
   var modelSetter = model.assign;

   element.bind('change', function() {
    scope.$apply(function() {
     modelSetter(scope, element[0].files[0]);
    });
   });
  }
 };
}]);

app.config(function($stateProvider, $urlRouterProvider) {

 $urlRouterProvider.otherwise('/home');

 $stateProvider
  .state('home', {
   url: '/home',
   templateUrl: '../view/login.html',
   controller: "TwitterLoginController"
  })
  .state('profile', {
   url: '/profile',
   templateUrl: '../view/profile.html',
   controller: 'GetBooksController'
  })
  .state('books', {
   url: '/books',
   templateUrl: '../view/bookDisplay.html',
   controller: 'GetBooksController'
  })
  .state('read', {
   url: '/read',
   templateUrl: '../view/textHolder.html',
   controller: 'GetBooksController'
  })
  .state('bookList', {
   url: '/bookList',
   templateUrl: '../view/bookList.html',
   controller: 'GetBooksController'
  })
  .state('search', {
   url: '/search',
   templateUrl: '../view/searchResults.html',
   controller: 'GetBooksController'
  })
  .state('admin', {
   url: '/admin',
   templateUrl: '../view/admin.html'
  });

});

//Controllers

app.controller('GetBooksController', ['$scope', '$rootScope', '$location', '$cookies', 'HomeService', function($scope, $rootScope, $location, $cookies, HomeService) {
 $scope.books = [];
 $scope.bookContent = null;
 $scope.book;
 $scope.chapter;
 $scope.quoteText = "";

 $scope.searchForm = {
  title: "",
  authorName: "",
  genre: ""
 };

 function init() {
  if ($cookies.get("token") == null || $cookies.get("token") == "undefined") {
   $location.path('/');
  }
 }
 init();

 $scope.getUser = function() {
  HomeService.getUser($cookies.get("token"))
   .then(function success(response) {
     $rootScope.user = response.data;
     $("#shareActivity").prop("checked", $rootScope.user.shareActivity);
    },
    function error(response) {
     console.log("Error getting user!");
    });

 };
 $scope.getBooks = function() {
  HomeService.getBooks($cookies.get("token"))
   .then(function success(response) {
     $scope.books = response.data;
    },
    function error(response) {
     console.log("Error getting books!");
    });
  $scope.getUser();
 };
 $scope.searchBooks = function() {
  var data = new FormData();
  HomeService.searchBooks($scope.searchForm.title, $scope.searchForm.authorName, $scope.searchForm.genre, $cookies.get("token"))
   .then(function success(response) {
     $location.path("/search");
     $rootScope.searchResults = response.data;
    },
    function error(response) {
     console.log(response);
    });
 };
 $scope.getBook = function(id) {
  HomeService.getBook(id, $cookies.get("token"))
   .then(function success(response) {
     $rootScope.book = response.data;
    },
    function error(response) {
     console.log(response);
    });
 };
 $scope.readMode = function(id, chapter) {
  $location.path("/read");
  $scope.getUser();
  if (chapter <= 1) {
   $("#prevButton").hide()
   HomeService.startBook(id, $cookies.get("token"));
  }
  if ($rootScope.book == null) {
   $scope.getBook(id);
  }
  $rootScope.bookId = id;
  $rootScope.chapter = chapter;
  if ($rootScope.bookContent == null) {
   HomeService.getText(id, $cookies.get("token"))
    .then(function success(response) {
      $rootScope.bookContent = response.data;
      document.getElementById("theContainer").innerHTML = $rootScope.bookContent[$rootScope.chapter];
     },
     function error(response) {
      console.log(response);
     });
  }
  document.getElementById("theContainer").innerHTML = $rootScope.bookContent[$rootScope.chapter];
  if (chapter <= 0) {
   $("#prevButton").hide()
  } else {
   $("#prevButton").show()
  }
  if (chapter >= $rootScope.bookContent.length - 1) {
   $("#nextButton").hide();
   HomeService.finishBook(id, $cookies.get("token"));
  } else {
   $("#nextButton").show();
  }
 };
 $scope.goBack = function() {
  $rootScope.bookContent = null;
  $rootScope.bookId = null;
  $rootScope.book = null;
  $rootScope.chapter = null;
  $location.path('/books');
 };
 $scope.getSelectedText = function() {
  $scope.quoteText = window.getSelection().toString();
 };
 $scope.addQuote = function() {

  if ($scope.quoteText.length > 0) {
   var quote = {
    content: $scope.quoteText,
    userId: $cookies.get("uid"),
    bookId: $rootScope.bookId
   };
   HomeService.addQuote(quote, $cookies.get("token"))
    .then(function success(response) {
      console.log("Successfully created quote!")
     },
     function error(response) {
      console.log("Error creating quote.");
     });
  }
 };

 $scope.searchInWikipedia = function() {
  window.open("/book/wikipedia?word=" + $scope.quoteText + "&token=" + $cookies.get("token"));
 };

 $scope.addBookList = function() {
  var bookList = {
   title: $scope.bookListForm.title,
   userId: $cookies.get("uid"),
   bookId: $rootScope.bookId
  };
  HomeService.addBookList(bookList, $cookies.get("token"))
   .then(function success(response) {
     console.log("Successfully created book list!")
    },
    function error(response) {
     console.log("Error creating book List.");
    });
 };

 $scope.addBookToBookList = function(bookListId, bookId) {
  HomeService.addBookToBookList(bookListId, bookId, $cookies.get("token"))
   .then(function success(response) {
     console.log("Successfully added book to book list!")
    },
    function error(response) {
     console.log("Error adding book to book list.");
    });

  $("#bookListHolder").hide();
 };
 $scope.goToProfile = function() {
  $scope.getUser();
  $location.path("/profile");
 };
 $scope.openBookList = function(id) {
  HomeService.getBookList(id, $cookies.get("token"))
   .then(function success(response) {
     $rootScope.bookList = response.data;
     $location.path("/bookList");
    },
    function error(response) {
     console.log(response);
    });
 };
 $scope.downloadBook = function(id) {
  window.open("http://localhost:8007/book/download?bookId=" + $rootScope.bookId + "&userId=" + $rootScope.user.id + "&token=" + $cookies.get("token"), '_blank');
 };
 $scope.toggleShareActivity = function() {
  HomeService.toggleShareActivity($cookies.get("token"))
   .then(function success(response) {
     $scope.getUser();
     console.log("Successfully toggled sharing activity!")
    },
    function error(response) {
     console.log("Error toggling sharing activity.");
    });
 };

}]);


app.controller('TwitterLoginController', ['$scope', '$location', '$rootScope', '$cookies', 'HomeService', function($scope, $location, $rootScope, $cookies, HomeService) {

 function init() {
  if ($cookies.get("token") != null && $cookies.get("token") != "undefined ") {
   $location.path('/books');
  }
 }
 init();

 $scope.login = function() {
  HomeService.login()
   .then(function success(response) {
     $scope.getUser($cookies.get("token"));
     console.log("Successfully logged in!");
     $location.path('/books');
    },
    function error(response) {
     $scope.getUser($cookies.get("token"));
     console.log("Error getting user!");
    });
 }
 $scope.getUser = function(token) {
  HomeService.getUser(token)
   .then(function success(response) {
     $rootScope.user = response.data;

    },
    function error(response) {
     $rootScope.user = response.data;
    });
 }

}]);

app.controller('AdminController', ['$scope', '$cookies', '$location', 'HomeService', function($scope, $cookies, $location, HomeService) {
 $scope.books = [];
 function init() {
   if ($cookies.get("token") == null || $cookies.get("token") == "undefined") {
    $location.path('/');
   }
   else{
       HomeService.confirmAdmin($cookies.get("token"))
          .then(function success(response) {
                console.log("Granted Access");
           },
           function error(response) {
                $location.path('/books');
           });
   }
  }
  init();

 $scope.getBooks = function() {
  HomeService.getBooks($cookies.get("token"))
   .then(function success(response) {
     $scope.books = response.data;
     $scope.message = '';
     $scope.errorMessage = '';
    },
    function error(response) {
     $scope.message = '';
     $scope.errorMessage = 'Error getting books!';
    });
 }
 $scope.deleteBook = function(id) {
  HomeService.deleteBook(id, $cookies.get("token"))
   .then(function success(response) {
     $scope.message = 'Book deleted!';
     $scope.book = null;
     $scope.errorMessage = '';

    },
    function error(response) {
     $scope.errorMessage = 'Error deleting book!';
     $scope.message = '';
    })
  HomeService.getBooks($cookies.get("token"));
  window.location.reload();
 }
}]);

app.controller('AddBookController', function($scope, $cookies, $http) {

 $scope.uploadResult = "";

 $scope.myForm = {
  title: "",
  description: "",
  authors: [],
  genre: "",
  imageFile: [],
  bookFile: []
 }

 $scope.addBook = function() {

  var url = "/book/?token=" + $cookies.get("token");


  var data = new FormData();
  data.append("title", $scope.myForm.title);
  data.append("description", $scope.myForm.description);
  for (i = 0; i < $scope.myForm.authors.split(", ").length; i++) {
   data.append("authors", $scope.myForm.authors.split(", ")[i]);
  }
  data.append("genre", $scope.myForm.genre);
  data.append("imageFile", $scope.myForm.imageFile);
  data.append("bookFile", $scope.myForm.bookFile);

  var config = {
   transformRequest: angular.identity,
   transformResponse: angular.identity,
   headers: {
    'Content-Type': undefined
   }
  }


  $http.post(url, data, config).then(
   // Success
   function(response) {
    console.log(response.data); //console.error
    location.reload();
   },
   // Error
   function(response) {
    console.log(response.data);
    location.reload();

   });
 };

});

app.controller('EditBookController', function($scope, $cookies, $http) {

 $scope.uploadResult = "";

 $scope.editForm = {
  title: "",
  description: "",
  authors: [],
  genre: ""
 }

 $scope.editBook = function(id) {

  var url = "/book/?id=" + id + "&token=" + $cookies.get("token");
  var data = new FormData();
  if ($scope.editForm.title.length > 0) {
   data.append("title", $scope.editForm.title);
  }
  if ($scope.editForm.description.length > 0) {
   data.append("description", $scope.editForm.description);
  }
  if ($scope.editForm.authors.length > 0) {
   for (i = 0; i < $scope.editForm.authors.split(", ").length; i++) {
    data.append("authors", $scope.editForm.authors.split(", ")[i]);
   }
  }
  if ($scope.editForm.genre.length > 0) {
   data.append("genre", $scope.editForm.genre);
  }
  var config = {
   transformRequest: angular.identity,
   transformResponse: angular.identity,
   headers: {
    'Content-Type': undefined
   }
  }


  $http.put(url, data, config).then(
   // Success
   function(response) {
    console.log(response.data);
    location.reload();
   },
   // Error
   function(response) {
    console.log(response.data);
    location.reload();

   });
 };

 $scope.editPicture = function(id) {
  var url = "/book/editPicture/?id=" + id + "&token=" + $cookies.get("token");
  var config = {
   transformRequest: angular.identity,
   transformResponse: angular.identity,
   headers: {
    'Content-Type': undefined
   }
  }

  var fd = new FormData();
  fd.append("image", $scope.editForm.imageFile);

  $http.put(url, fd, config).then(
   // Success
   function(response) {
    console.log(response.data);
    location.reload();
   },
   // Error
   function(response) {
    console.log(response.data);
    location.reload();

   });
 };

 $scope.editFile = function(id) {
  var url = "/book/editFile/?id=" + id + "&token=" + $cookies.get("token");
  var fd = new FormData();
  fd.append("file", $scope.editForm.bookFile);
  var config = {
   transformRequest: angular.identity,
   transformResponse: angular.identity,
   headers: {
    'Content-Type': undefined
   }
  }


  $http.put(url, fd, config).then(
   // Success
   function(response) {
    console.log(response.data);
    location.reload();
   },
   // Error
   function(response) {
    console.log(response.data);
    location.reload();

   });
 };

});

//Service

app.service('HomeService', ['$http', function($http) {
 this.getUser = function getUser(token) {
  return $http({
   method: 'GET',
   url: 'user/?token=' + token
  });
 }
 this.getBook = function getBook(id, token) {
  return $http({
   method: 'GET',
   url: '/book/' + id + '?token=' + token
  });
 }
 this.getBooks = function getBooks(token) {
  return $http({
   method: 'GET',
   url: '/book/' + '?token=' + token
  });
 }
 this.searchBooks = function searchBooks(title, authorName, genre, token) {
  return $http({
   method: 'GET',
   url: '/book/search?title=' + title + '&authorName=' + authorName + '&genre=' + genre + '&token=' + token
  });
 }
 this.getText = function getText(id, token) {
  return $http({
   method: 'GET',
   url: '/book/getText?id=' + id + '&token=' + token
  });
 }
 this.getBookList = function getBookList(id, token) {
  return $http({
   method: 'GET',
   url: '/bookList/' + id + '?token=' + token
  });
 }
 this.addQuote = function addQuote(quote, token) {
  return $http({
   method: 'POST',
   url: '/quote/' + '?token=' + token,
   contentType: 'application/json',
   dataType: 'json',
   data: quote
  });
 }
 this.addBookList = function addBookList(bookList, token) {
  return $http({
   method: 'POST',
   url: '/bookList/' + '?token=' + token,
   contentType: 'application/json',
   dataType: 'json',
   data: bookList
  });
 }
 this.addBookToBookList = function addBookToBookList(bookListId, bookId, token) {
  return $http({
   method: 'PUT',
   url: '/bookList/?bookListId=' + bookListId + '&bookId=' + bookId + '&token=' + token
  });
 }
 this.startBook = function startBook(id, token) {
  return $http({
   method: 'PUT',
   url: 'user/startBook?bookId=' + id + '&token=' + token
  });
 }
 this.finishBook = function finishBook(id, token) {
  return $http({
   method: 'PUT',
   url: 'user/finishBook?bookId=' + id + '&token=' + token
  });
 }
 this.toggleShareActivity = function toggleShareActivity(token) {
  return $http({
   method: 'PUT',
   url: 'user/toggleShareActivity?token=' + token
  });
 }
 this.login = function login() {
  return $http({
   method: 'GET',
   url: 'user/getToken'
  });
 }
 this.deleteBook = function deleteBook(id, token) {
  return $http({
   method: 'DELETE',
   url: 'book/?id=' + id + "&token=" + token
  })
 }
 this.confirmAdmin = function confirmAdmin(token) {
   return $http({
    method: 'GET',
    url: 'user/confirm?token=' + token
   })
  }
}]);

function getAuthorsAsString(authors) {
 var result = "";
 for (var i = 0; i < authors.length; i++) {
  result.concat(authors[i].name);
 }
 return result;
}
