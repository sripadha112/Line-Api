#!/bin/bash
# Automatic Push Notification System - Test Script
# Run this after starting your Spring Boot application

echo "🚀 Testing Automatic Push Notification System"
echo "=============================================="

BASE_URL="http://localhost:8080"
USER_ID="1"
TEST_FCM_TOKEN="test_fcm_token_12345"

echo ""
echo "1️⃣  Testing FCM Token Registration..."
curl -X POST "$BASE_URL/api/user/$USER_ID/fcm-token" \
  -H "Content-Type: application/json" \
  -d "{
    \"fcmToken\": \"$TEST_FCM_TOKEN\",
    \"deviceType\": \"android\"
  }" | jq .

echo ""
echo "2️⃣  Testing Notification Settings Toggle..."
curl -X PUT "$BASE_URL/api/user/$USER_ID/notifications/toggle?enabled=true" | jq .

echo ""
echo "3️⃣  Testing Manual FCM Notification..."
curl -X POST "$BASE_URL/api/notifications/notify/simple" \
  -H "Content-Type: application/json" \
  -d "{
    \"deviceToken\": \"$TEST_FCM_TOKEN\",
    \"title\": \"🏥 Test Notification\",
    \"body\": \"Your automatic push notification system is working!\"
  }" | jq .

echo ""
echo "4️⃣  Testing Android-Specific Notification..."
curl -X POST "$BASE_URL/api/notifications/notify/android" \
  -H "Content-Type: application/json" \
  -d "{
    \"deviceToken\": \"$TEST_FCM_TOKEN\",
    \"title\": \"📱 Android System Notification\",
    \"body\": \"This should appear in your Android notification tray\",
    \"androidConfig\": {
      \"channelId\": \"appointment_updates\",
      \"priority\": \"high\",
      \"sound\": \"default\"
    },
    \"data\": {
      \"type\": \"TEST_ANDROID\",
      \"userId\": \"$USER_ID\"
    }
  }" | jq .

echo ""
echo "5️⃣  Testing iOS-Specific Notification..."
curl -X POST "$BASE_URL/api/notifications/notify/ios" \
  -H "Content-Type: application/json" \
  -d "{
    \"deviceToken\": \"$TEST_FCM_TOKEN\",
    \"title\": \"🍎 iOS System Notification\",
    \"body\": \"This should appear in your iOS notification center\",
    \"iosConfig\": {
      \"sound\": \"default\",
      \"badge\": 1,
      \"contentAvailable\": true
    },
    \"data\": {
      \"type\": \"TEST_IOS\",
      \"userId\": \"$USER_ID\"
    }
  }" | jq .

echo ""
echo "✅ Testing Complete!"
echo ""
echo "📋 Manual Testing Steps:"
echo "   1. Book an appointment → Should get confirmation notification"
echo "   2. Cancel appointment → Should get cancellation notification"  
echo "   3. Doctor updates status → Should get update notification"
echo ""
echo "🔧 To test with real device FCM token:"
echo "   1. Get FCM token from your mobile app"
echo "   2. Replace TEST_FCM_TOKEN in this script"
echo "   3. Run script again"
echo ""
echo "📱 Mobile App Integration:"
echo "   - Call POST /api/user/{userId}/fcm-token on app startup"
echo "   - Handle notification data payload for app navigation"
echo "   - Configure notification channels/permissions properly"