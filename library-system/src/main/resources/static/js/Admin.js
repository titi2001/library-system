var app = angular.module('AdminApp',[]);
app.directive('fileModel', ['$parse', function ($parse) {
    return {
       restrict: 'A',
       link: function(scope, element, attrs) {
          var model = $parse(attrs.fileModel);
          var modelSetter = model.assign;

          element.bind('change', function(){
             scope.$apply(function(){
                modelSetter(scope, element[0].files[0]);
             });
          });
       }
    };
}]);
app.controller('AddBookController', function($scope, $http) {

    $scope.uploadResult ="";

    $scope.myForm = {
        title: "",
        description: "",
        authors: [],
        genre: "",
        imageFile: [],
        bookFile: []
    }

    $scope.addBook = function() {

        var url = "/book/add";


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

app.controller('EditBookController', function($scope, $http) {

    $scope.uploadResult ="";

    $scope.editForm = {
        title: "",
        description: "",
        authors: [],
        genre: ""
    }

    $scope.editBook = function(id) {

        var url = "/book/edit/" + id;
        var data = new FormData();
        if($scope.editForm.title.length > 0){data.append("title", $scope.editForm.title);}
        if($scope.editForm.description.length > 0){data.append("description", $scope.editForm.description);}
        if($scope.editForm.authors.length > 0){
        for (i = 0; i < $scope.editForm.authors.split(", ").length; i++) {
            data.append("authors", $scope.editForm.authors.split(", ")[i]);
        }}
        if($scope.editForm.genre.length > 0){
        data.append("genre", $scope.editForm.genre);}
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

    $scope.editPicture = function(id){
            var url = "/book/editPicture/" + id;
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

    $scope.editFile = function(id){
                var url = "/book/editFile/" + id;
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


app.controller('AdminController', ['$scope','AdminService', function ($scope,AdminService) {
    $scope.books = [];
    $scope.getBooks = function () {
        AdminService.getBooks()
          .then(function success(response){
              $scope.books = response.data;
              $scope.message='';
              $scope.errorMessage = '';
          },
          function error (response ){
              $scope.message='';
              $scope.errorMessage = 'Error getting books!';
          });
    }
    $scope.deleteBook = function (id) {
            AdminService.deleteBook(id)
              .then (function success(response){
                  $scope.message = 'Book deleted!';
                  $scope.book = null;
                  $scope.errorMessage='';

              },
              function error(response){
                  $scope.errorMessage = 'Error deleting book!';
                  $scope.message='';
              })
              window.location.reload();
              window.location.reload();
        }
}]);

app.service('AdminService',['$http', function ($http) {


    this.getBooks = function getBooks(name){
        return $http({
          method: 'GET',
          url: '/book/getAll'
        });
    }
    this.deleteBook = function deleteBook(id){
            return $http({
              method: 'DELETE',
              url: 'book/delete/' + id
            })
        }
}]);


function getAuthorsAsString(authors){
    var result = "";
    for(var i = 0; i < authors.length; i++){
        result.concat(authors[i].name);
    }
    return result;
    }