#!/usr/bin/env bash
#
# Sample usage:
#   ./test_library_ms.bash start stop
#   start and stop are optional
#
#   HOST=localhost PORT=8080 ./test_library_ms.bash
#
: ${HOST=localhost}
: ${PORT=8080}

# arrays to hold all our test data ids
allTestDepartmentIds=()
allTestEmployeeIds=()
allTestPatronIds=()
allTestBookIds=()
allTestLoanIds=()

function assertCurl() {
  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]; then
    if [ "$httpCode" = "200" ]; then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
  else
    echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
    echo  "- Failing command: $curlCmd"
    echo  "- Response Body: $RESPONSE"
    exit 1
  fi
}

function assertEqual() {
  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]; then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}

# have all the microservices come up yet?
function testUrl() {
  url=$@
  if curl $url -ks -f -o /dev/null; then
    echo "Ok"
    return 0
  else
    echo -n "not yet"
    return 1
  fi
}

# prepare the test data that will be passed in the curl commands for posts and puts
function setupTestdata() {
  # USE SEEDED DEPARTMENT ID (from data-h2.sql)
  allTestDepartmentIds[1]="1048b354-c18f-4109-8282-2a85485bfa5a"
  echo "Using seeded Department with departmentId: ${allTestDepartmentIds[1]}"

  # CREATE SOME EMPLOYEE TEST DATA
  body='{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jdoe@example.com",
    "phoneNumbers": [
      {"type":"WORK","number":"514-123-4567"}
    ],
    "streetAddress":"100 Library Ave",
    "city":"Montreal",
    "province":"QC",
    "country":"Canada",
    "postalCode":"H2X1Y4",
    "salary":50000.00,
    "departmentId":"'"${allTestDepartmentIds[1]}"'",
    "positionTitle":"LIBRARIAN"
  }'
  recreateEmployeeAggregate 1 "$body"

  # CREATE SOME PATRON TEST DATA
  body='{
    "firstName":"John",
    "lastName":"Smith",
    "email":"jsmith@example.com",
    "password":"secret",
    "memberShipType":"REGULAR",
    "phoneNumbers":[{"type":"MOBILE","number":"450-987-6543"}],
    "streetAddress":"200 Book St",
    "city":"Montreal",
    "province":"QC",
    "country":"Canada",
    "postalCode":"H2X1Y4"
  }'
  recreatePatronAggregate 1 "$body"

  # CREATE SOME BOOK TEST DATA
  body='{
    "isbn":"1234567890",
    "title":"Test Book",
    "authorFirstName":"Alice",
    "authorLastName":"Walker",
    "authorBiography":"Bio",
    "genre":"FICTION",
    "publicationDate":"2025-01-01",
    "bookType":"HARDCOVER",
    "ageGroup":"ADULT",
    "copiesAvailable":3
  }'
  recreateBookAggregate 1 "$body"

  # CREATE SOME LOAN REQUEST TEST DATA
  # all use patronId ${allTestPatronIds[1]}
  body='{
    "patronId":"123e4567-e89b-12d3-a456-426614174000",
    "bookId":"6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a",
    "employeeId":"e8a17e76-1c9f-4a6a-9342-488b7e99f0f7",
    "issueDate":"2025-05-10",
    "checkoutDate":"2025-05-11",
    "returnDate":"2025-05-20",
    "status":"CHECKED_OUT"
  }'
  recreateLoanAggregate 1 "$body" "123e4567-e89b-12d3-a456-426614174000"
} # end setupTestdata

function recreateEmployeeAggregate() {
  local testId=$1 aggregate=$2
  employeeId=$(curl -s -X POST http://$HOST:$PORT/api/v1/staff \
    -H "Content-Type: application/json" --data "$aggregate" | jq -r '.employeeId')
  allTestEmployeeIds[$testId]=$employeeId
  echo "Added Employee with employeeId: ${allTestEmployeeIds[$testId]}"
}

function recreatePatronAggregate() {
  local testId=$1 aggregate=$2
  patId=$(curl -s -X POST http://$HOST:$PORT/api/v1/patrons \
    -H "Content-Type: application/json" \
    --data "$aggregate" \
    | jq -r '.patronId')
  allTestPatronIds[$testId]=$patId
  echo "Added Patron with patronId: ${allTestPatronIds[$testId]}"
}

function recreateBookAggregate() {
  local testId=$1 aggregate=$2
  bookId=$(curl -s -X POST http://$HOST:$PORT/api/v1/books \
    -H "Content-Type: application/json" --data "$aggregate" | jq -r '.bookId')
  allTestBookIds[$testId]=$bookId
  echo "Added Book with bookId: ${allTestBookIds[$testId]}"
}

function recreateLoanAggregate() {
  local testId=$1 aggregate=$2 patronId=$3
  loanId=$(curl -s -X POST http://$HOST:$PORT/api/v1/patrons/${patronId}/loans \
    -H "Content-Type: application/json" --data "$aggregate" | jq -r '.loanId')
  allTestLoanIds[$testId]=$loanId
  echo "Added Loan with loanId: ${allTestLoanIds[$testId]}"
}

# don't start testing until all the microservices are up and running
function waitForService() {
  url=$@
  echo -n "Wait for: $url... "
  n=0
  until testUrl $url; do
    n=$((n + 1))
    if [[ $n == 100 ]]; then
      echo " Give up"
      exit 1
    else
      sleep 6
      echo -n ", retry #$n "
    fi
  done
  echo " Done"
}

# start of test script
set -e

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]; then
  echo "Restarting the test environment..."
  docker-compose down
  docker-compose up -d
fi

# ensure gateway & backing services are ready
waitForService curl -X GET http://$HOST:$PORT/api/v1/departments
waitForService curl -X GET http://$HOST:$PORT/api/v1/staff
waitForService curl -X GET http://$HOST:$PORT/api/v1/patrons
waitForService curl -X GET http://$HOST:$PORT/api/v1/books


setupTestdata

# EXECUTE EXPLICIT TESTS AND VALIDATE RESPONSES
#
# STAFF (Employees)
#
# Test 1: Get All Employees
echo -e "\nTest 1: Verify that a GET ALL staff works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/staff -s"

# Test 2: Get Employee By Id
echo -e "\nTest 2: Verify that a GET by employeeId of earlier posted employee works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/staff/${allTestEmployeeIds[1]} -s"

# Test 3: 404 for non-existing employeeId
echo -e "\nTest 3: Verify that a 404 is returned for non-existing employeeId"
assertCurl 404 "curl http://$HOST:$PORT/api/v1/staff/c3540a89-cb47-4c96-888e-ff96708db4d7 -s"

# Test 4: 422 for invalid employeeId
echo -e "\nTest 4: Verify that a 422 is returned for invalid employeeId"
assertCurl 422 "curl http://$HOST:$PORT/api/v1/staff/c3540a89-cb47-4c96-888e-ff9670 -s"

# Test 5: POST a new employee
echo -e "\nTest 5: Verify that a POST of an employee works"
body='{
  "firstName": "Sean",
  "lastName": "Connery",
  "email": "sconnery@example.com",
  "phoneNumbers": [{"type":"WORK","number":"555-555-2323"}],
  "streetAddress": "1190 Church Street",
  "city": "Montreal",
  "province": "QC",
  "country": "Canada",
  "postalCode": "H1A 0A4",
  "salary": 60000.00,
  "commissionRate": 5.0,
  "departmentId": "'"${allTestDepartmentIds[1]}"'",
  "positionTitle": "LIBRARIAN"
}'
assertCurl 201 "curl -X POST http://$HOST:$PORT/api/v1/staff -H \"Content-Type: application/json\" -d '${body}' -s"

firstName=$(echo "$RESPONSE" | jq -r '.firstName')
assertEqual "Sean" "$firstName"

phoneCount=$(echo "$RESPONSE" | jq -r '.phoneNumbers | length')
assertEqual "1" "$phoneCount"


# Test 6: PUT update existing employee
echo -e "\nTest 6: Verify that a PUT of an earlier posted employee works"
body='{
  "firstName": "Christine2",
  "lastName": "Gerard",
  "email": "cgerard@example.com",
  "phoneNumbers": [
    {"type":"MOBILE","number":"450-555-3456"},
    {"type":"WORK","number":"555-555-5555"}
  ],
  "streetAddress": "123 Main Street",
  "city": "Montreal",
  "province": "QC",
  "country": "Canada",
  "postalCode": "H1A 0A4",
  "salary": 56000.00,
  "commissionRate": 5.0,
  "departmentId": "'"${allTestDepartmentIds[1]}"'",
  "positionTitle": "LIBRARIAN"
}'
assertCurl 200 "curl -X PUT http://$HOST:$PORT/api/v1/staff/${allTestEmployeeIds[1]} -H \"Content-Type: application/json\" -d '${body}' -s"

empId=$(echo "$RESPONSE" | jq -r '.employeeId')
assertEqual "${allTestEmployeeIds[1]}" "$empId"

firstName=$(echo "$RESPONSE" | jq -r '.firstName')
assertEqual "Christine2" "$firstName"

# Test 7: 404 for PUT on non-existing employeeId
echo -e "\nTest 7: Verify that a 404 is returned for PUT on non-existing employeeId"
assertCurl 404 "curl -X PUT http://$HOST:$PORT/api/v1/staff/c3540a89-cb47-4c96-888e-ff96708db4d7 -H \"Content-Type: application/json\" -d '${body}' -s"

# Test 8: 422 for PUT on invalid employeeId
echo -e "\nTest 8: Verify that a 422 is returned for PUT on invalid employeeId"
assertCurl 422 "curl -X PUT http://$HOST:$PORT/api/v1/staff/c3540a89-cb47-4c96-888e-ff9670 -H \"Content-Type: application/json\" -d '${body}' -s"

# Test 9: DELETE existing employee
echo -e "\nTest 9: Verify that DELETE of earlier posted employee works"
assertCurl 204 "curl -X DELETE http://$HOST:$PORT/api/v1/staff/${allTestEmployeeIds[1]} -s"

# Test 10: 404 for DELETE on non-existing employeeId
echo -e "\nTest 10: Verify that a 404 is returned for DELETE on non-existing employeeId"
assertCurl 404 "curl -X DELETE http://$HOST:$PORT/api/v1/staff/c3540a89-cb47-4c96-888e-ff96708db4d7 -s"

# Test 11: 422 for DELETE on invalid employeeId
echo -e "\nTest 11: Verify that a 422 is returned for DELETE on invalid employeeId"
assertCurl 422 "curl -X DELETE http://$HOST:$PORT/api/v1/staff/c3540a89-cb47-4c96-888e-ff9670 -s"

# === PATRONS ===

# Test P1: Verify that a GET ALL patrons works
echo -e "\nTest P1: Verify that a GET ALL patrons works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/patrons -s"
patronCount=$(echo "$RESPONSE" | jq -r 'length')
assertEqual "11" "$patronCount"

# Test P2: Verify that a GET by patronId of earlier posted patron works
echo -e "\nTest P2: Verify that a GET by patronId of earlier posted patron works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/patrons/${allTestPatronIds[1]} -s"
patId=$(echo "$RESPONSE" | jq -r '.patronId')
assertEqual "${allTestPatronIds[1]}" "$patId"


# Test P3: Verify that a 404 is returned for non-existing patronId
echo -e "\nTest P3: Verify that a 404 is returned for non-existing patronId"
assertCurl 404 "curl http://$HOST:$PORT/api/v1/patrons/c3540a89-cb47-4c96-888e-ff96708db4d7 -s"

# Test P4: Verify that a 422 is returned for invalid patronId
echo -e "\nTest P4: Verify that a 422 is returned for invalid patronId"
assertCurl 422 "curl http://$HOST:$PORT/api/v1/patrons/c3540a89-cb47-4c96-888e-ff9670 -s"

# Test P5: Verify that a POST of a patron works
echo -e "\nTest P5: Verify that a POST of a patron works"
body='{
  "firstName":"Alice",
  "lastName":"Wonder",
  "email":"alice@example.com",
  "password":"secret",
  "memberShipType":"PREMIUM",
  "phoneNumbers":[{"type":"MOBILE","number":"123-456-7890"}],
  "streetAddress":"123 Garden St",
  "city":"Montreal",
  "province":"QC",
  "country":"Canada",
  "postalCode":"H1A1A1"
}'
assertCurl 201 "curl -X POST http://$HOST:$PORT/api/v1/patrons -H \"Content-Type: application/json\" -d '${body}' -s"
firstName=$(echo "$RESPONSE" | jq -r '.firstName')
assertEqual "Alice" "$firstName"
phoneCount=$(echo "$RESPONSE" | jq -r '.phoneNumbers | length')
assertEqual "1" "$phoneCount"

# Test P6: Verify that a PUT of an earlier posted patron works
echo -e "\nTest P6: Verify that a PUT of an earlier posted patron works"
body='{
  "firstName":"Alice2",
  "lastName":"Wonderland",
  "email":"alice2@example.com",
  "password":"secret2",
  "memberShipType":"REGULAR",
  "phoneNumbers":[{"type":"HOME","number":"999-999-9999"}],
  "streetAddress":"321 Garden St",
  "city":"Montreal",
  "province":"QC",
  "country":"Canada",
  "postalCode":"H1A1A1"
}'
assertCurl 200 "curl -X PUT http://$HOST:$PORT/api/v1/patrons/${allTestPatronIds[1]} -H \"Content-Type: application/json\" -d '${body}' -s"
updatedFirstName=$(echo "$RESPONSE" | jq -r '.firstName')
assertEqual "Alice2" "$updatedFirstName"
updatedPhoneCount=$(echo "$RESPONSE" | jq -r '.phoneNumbers | length')
assertEqual "1" "$updatedPhoneCount"

# Test P7: Verify that a 404 is returned for PUT on non-existing patronId
echo -e "\nTest P7: Verify that a 404 is returned for PUT on non-existing patronId"
assertCurl 404 "curl -X PUT http://$HOST:$PORT/api/v1/patrons/c3540a89-cb47-4c96-888e-ff96708db4d7 -H \"Content-Type: application/json\" -d '${body}' -s"

# Test P8: Verify that a 422 is returned for PUT on invalid patronId
echo -e "\nTest P8: Verify that a 422 is returned for PUT on invalid patronId"
assertCurl 422 "curl -X PUT http://$HOST:$PORT/api/v1/patrons/c3540a89-cb47-4c96-888e-ff9670 -H \"Content-Type: application/json\" -d '${body}' -s"

# Test P9: Verify that DELETE of earlier posted patron works
echo -e "\nTest P9: Verify that DELETE of earlier posted patron works"
assertCurl 204 "curl -X DELETE http://$HOST:$PORT/api/v1/patrons/${allTestPatronIds[1]} -s"

# Test P10: Verify that a 404 is returned for DELETE on non-existing patronId
echo -e "\nTest P10: Verify that a 404 is returned for DELETE on non-existing patronId"
assertCurl 404 "curl -X DELETE http://$HOST:$PORT/api/v1/patrons/c3540a89-cb47-4c96-888e-ff96708db4d7 -s"

# Test P11: Verify that a 422 is returned for DELETE on invalid patronId
echo -e "\nTest P11: Verify that a 422 is returned for DELETE on invalid patronId"
assertCurl 422 "curl -X DELETE http://$HOST:$PORT/api/v1/patrons/c3540a89-cb47-4c96-888e-ff9670 -s"

# === BOOKS ===

# Test B1: Verify that a GET ALL books works
echo -e "\nTest B1: Verify that a GET ALL books works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/books -s"
# make sure we have at least one book
bookCount=$(echo "$RESPONSE" | jq -r 'length')
if [ "$bookCount" -lt 1 ]; then
  echo "Test FAILED, expected >=1 books but got $bookCount"
  exit 1
else
  echo "Test OK (found $bookCount books)"
fi

# Test B2: Verify that a GET by bookId of earlier posted book works
echo -e "\nTest B2: Verify that a GET by bookId of earlier posted book works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/books/${allTestBookIds[1]} -s"
gotBookId=$(echo "$RESPONSE" | jq -r '.bookId')
assertEqual "${allTestBookIds[1]}" "$gotBookId"

# Test B3: Verify that a 404 is returned for non-existing bookId
echo -e "\nTest B3: Verify that a 404 is returned for non-existing bookId"
assertCurl 404 "curl http://$HOST:$PORT/api/v1/books/00000000-0000-0000-0000-000000000000 -s"

# Test B4: Verify that a 422 is returned for invalid bookId
echo -e "\nTest B4: Verify that a 422 is returned for invalid bookId"
assertCurl 422 "curl http://$HOST:$PORT/api/v1/books/invalid-id -s"

# Test B5: Verify that a POST of a book works
echo -e "\nTest B5: Verify that a POST of a book works"
bookBody='{
  "isbn":"0987654321",
  "title":"Another Test Book",
  "authorFirstName":"Bob",
  "authorLastName":"Marley",
  "authorBiography":"Singer",
  "genre":"FICTION",
  "publicationDate":"2025-02-02",
  "bookType":"PAPERBACK",
  "ageGroup":"ADULT",
  "copiesAvailable":5
}'
assertCurl 201 "curl -X POST http://$HOST:$PORT/api/v1/books -H \"Content-Type: application/json\" -d '$bookBody' -s"
newBookId=$(echo "$RESPONSE" | jq -r '.bookId')
assertEqual "0987654321" "$(echo "$RESPONSE" | jq -r '.isbn')"
assertEqual "Another Test Book" "$(echo "$RESPONSE" | jq -r '.title')"

# Test B6: Verify that a PUT of an earlier posted book works
echo -e "\nTest B6: Verify that a PUT of an earlier posted book works"
putBody='{
  "isbn":"0987654321",
  "title":"Updated Test Book",
  "authorFirstName":"Bob",
  "authorLastName":"Marley",
  "authorBiography":"Legend",
  "genre":"FICTION",
  "publicationDate":"2025-02-02",
  "bookType":"PAPERBACK",
  "ageGroup":"ADULT",
  "copiesAvailable":7
}'
assertCurl 200 "curl -X PUT http://$HOST:$PORT/api/v1/books/${newBookId} -H \"Content-Type: application/json\" -d '$putBody' -s"
assertEqual "${newBookId}" "$(echo "$RESPONSE" | jq -r '.bookId')"
assertEqual "Updated Test Book" "$(echo "$RESPONSE" | jq -r '.title')"
assertEqual "7" "$(echo "$RESPONSE" | jq -r '.copiesAvailable')"

# Test B7: Verify that a 404 is returned for PUT on non-existing bookId
echo -e "\nTest B7: Verify that a 404 is returned for PUT on non-existing bookId"
assertCurl 404 "curl -X PUT http://$HOST:$PORT/api/v1/books/00000000-0000-0000-0000-000000000000 -H \"Content-Type: application/json\" -d '$putBody' -s"

# Test B8: Verify that a 422 is returned for PUT on invalid bookId
echo -e "\nTest B8: Verify that a 422 is returned for PUT on invalid bookId"
assertCurl 422 "curl -X PUT http://$HOST:$PORT/api/v1/books/invalid-id -H \"Content-Type: application/json\" -d '$putBody' -s"

# Test B9: Verify that DELETE of earlier posted book works
echo -e "\nTest B9: Verify that DELETE of earlier posted book works"
assertCurl 204 "curl -X DELETE http://$HOST:$PORT/api/v1/books/${newBookId} -s"

# Test B10: Verify that a 404 is returned for DELETE on non-existing bookId
echo -e "\nTest B10: Verify that a 404 is returned for DELETE on non-existing bookId"
assertCurl 404 "curl -X DELETE http://$HOST:$PORT/api/v1/books/00000000-0000-0000-0000-000000000000 -s"

# Test B11: Verify that a 422 is returned for DELETE on invalid bookId
echo -e "\nTest B11: Verify that a 422 is returned for DELETE on invalid bookId"
assertCurl 422 "curl -X DELETE http://$HOST:$PORT/api/v1/books/invalid-id -s"

# === LOANS ===

# Test L1: Verify that a GET ALL loans for a patron works
echo -e "\nTest L1: Verify that a GET ALL loans for a patron works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/patrons/123e4567-e89b-12d3-a456-426614174000/loans -s"
loanCount=$(echo "$RESPONSE" | jq -r 'length')
assertEqual "2" "$loanCount"

# Test L2: Verify that a GET by loanId of earlier posted loan works
echo -e "\nTest L2: Verify that a GET by loanId of earlier posted loan works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/patrons/123e4567-e89b-12d3-a456-426614174000/loans/${allTestLoanIds[1]} -s"
gotLoanId=$(echo "$RESPONSE" | jq -r '.loanId')
assertEqual "${allTestLoanIds[1]}" "$gotLoanId"

# Test L3: Verify that a 404 is returned for non-existing patronId on loans
echo -e "\nTest L3: Verify that a 404 is returned for non-existing patronId"
assertCurl 404 "curl http://$HOST:$PORT/api/v1/patrons/00000000-0000-0000-0000-000000000000/loans/${allTestLoanIds[1]} -s"

# Test L4: Verify that a 422 is returned for invalid patronId on loans
echo -e "\nTest L4: Verify that a 422 is returned for invalid patronId"
assertCurl 422 "curl http://$HOST:$PORT/api/v1/patrons/invalid-id/loans/${allTestLoanIds[1]} -s"

# Test L5: Verify that a 404 is returned for non-existing loanId
echo -e "\nTest L5: Verify that a 404 is returned for non-existing loanId"
assertCurl 404 "curl http://$HOST:$PORT/api/v1/patrons/123e4567-e89b-12d3-a456-426614174000/loans/00000000-0000-0000-0000-000000000000 -s"

# Test L6: Verify that a 422 is returned for invalid loanId
echo -e "\nTest L6: Verify that a 422 is returned for invalid loanId"
assertCurl 422 "curl http://$HOST:$PORT/api/v1/patrons/123e4567-e89b-12d3-a456-426614174000/loans/invalid-id -s"

# Test L7: Verify that a PUT to update loan status works
# NOTE: we must send the full LoanRequestModel, not just the changed fields.
echo -e "\nTest L7: Verify that a PUT to update loan status works"
updateBody='{
  "patronId": "123e4567-e89b-12d3-a456-426614174000",
  "bookId":   "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a",
  "employeeId":"e8a17e76-1c9f-4a6a-9342-488b7e99f0f7",
  "issueDate":"2025-05-10",
  "checkoutDate":"2025-05-11",
  "returnDate":"2025-05-18",
  "status":"RETURNED"
}'
assertCurl 200 "curl -X PUT http://$HOST:$PORT/api/v1/patrons/123e4567-e89b-12d3-a456-426614174000/loans/2d8d1a47-08d8-4598-8b9d-6b2ec67dee1d \
  -H \"Content-Type: application/json\" \
  -d '$updateBody' -s"
updatedStatus=$(echo "$RESPONSE" | jq -r '.status')
assertEqual "RETURNED" "$updatedStatus"

# Test L8: Verify that a 404 is returned for PUT on non-existing loan
echo -e "\nTest L8: Verify that a 404 is returned for PUT on non-existing loanId"
assertCurl 404 "curl -X PUT http://$HOST:$PORT/api/v1/patrons/123e4567-e89b-12d3-a456-426614174000/loans/00000000-0000-0000-0000-000000000000 -H \"Content-Type: application/json\" -d '$updateBody' -s"

# Test L9: Verify that a 422 is returned for PUT on invalid loanId
echo -e "\nTest L9: Verify that a 422 is returned for PUT on invalid loanId"
assertCurl 422 "curl -X PUT http://$HOST:$PORT/api/v1/patrons/123e4567-e89b-12d3-a456-426614174000/loans/invalid-id -H \"Content-Type: application/json\" -d '$updateBody' -s"

# Test L10: Verify that DELETE of earlier posted loan works
echo -e "\nTest L10: Verify that DELETE of earlier posted loan works"
assertCurl 204 "curl -X DELETE http://$HOST:$PORT/api/v1/patrons/123e4567-e89b-12d3-a456-426614174000/loans/${allTestLoanIds[1]} -s"

# Test L11: Verify that a 404 is returned for DELETE on non-existing loanId
echo -e "\nTest L11: Verify that a 404 is returned for DELETE on non-existing loanId"
assertCurl 404 "curl -X DELETE http://$HOST:$PORT/api/v1/patrons/123e4567-e89b-12d3-a456-426614174000/loans/00000000-0000-0000-0000-000000000000 -s"

# Test L0: Verify that a POST of a new loan works
echo -e "\nTest L0: Verify that a POST of a new loan works"
postBody='{
  "patronId": "123e4567-e89b-12d3-a456-426614174000",
    "bookId":   "6fa459ea-ee8a-3ca4-894e-d6f1d55b4f2a",
    "employeeId":"e8a17e76-1c9f-4a6a-9342-488b7e99f0f7",
    "issueDate":"2025-05-10",
    "checkoutDate":"2025-05-11",
    "returnDate":"2025-05-18",
  "status":"CHECKED_OUT"
}'
# send POST and expect 201 Created
assertCurl 201 "curl -X POST http://$HOST:$PORT/api/v1/patrons/123e4567-e89b-12d3-a456-426614174000/loans \
  -H \"Content-Type: application/json\" \
  -d '$postBody' -s"

# grab the new loanId and stash it for later tests
newLoanId=$(echo "$RESPONSE" | jq -r '.loanId')
allTestLoanIds[1]=$newLoanId
echo "Created Loan with loanId: $newLoanId"

# sanity-check a couple of fields
assertEqual "123e4567-e89b-12d3-a456-426614174000" "$(echo "$RESPONSE" | jq -r '.patronId')"
assertEqual "CHECKED_OUT"        "$(echo "$RESPONSE" | jq -r '.status')"


# cleanup docker
if [[ $@ == *"stop"* ]]; then
  echo "We are done, stopping the test environment..."
  docker-compose down
fi
