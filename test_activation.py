#!/usr/bin/env python3
"""
Script de test pour la fonctionnalité d'activation des comptes
"""
import requests
import json

BASE_URL = "http://localhost:8082/api"

def test_create_user():
    """Test 1: Admin crée un utilisateur encadrant"""
    print("\n" + "="*60)
    print("TEST 1: Création d'un utilisateur par l'admin")
    print("="*60)
    
    # D'abord, login admin
    login_data = {
        "email": "admin@gmail.com",
        "password": "admin123"  # À adapter selon votre mot de passe
    }
    
    try:
        response = requests.post(f"{BASE_URL}/auth/login", json=login_data)
        print(f"\nLogin Admin - Status: {response.status_code}")
        
        if response.status_code == 200:
            token = response.json()['token']
            print(f"✅ Admin connecté avec succès")
            
            # Créer un nouvel utilisateur
            headers = {"Authorization": f"Bearer {token}"}
            new_user = {
                "firstName": "Test",
                "lastName": "Encadrant",
                "email": "test.encadrant@gmail.com",
                "phone": "0612345678",
                "role": "ENCADRANT",
                "address": "Casablanca"
            }
            
            response = requests.post(
                f"{BASE_URL}/admin/users",
                headers=headers,
                json=new_user
            )
            
            print(f"\nCréation utilisateur - Status: {response.status_code}")
            
            if response.status_code == 201:
                user = response.json()
                print(f"✅ Utilisateur créé avec succès!")
                print(f"   ID: {user['id']}")
                print(f"   Email: {user['email']}")
                print(f"   Rôle: {user['role']}")
                print(f"   Account Status: {user['accountStatus']}")
                print(f"   Actif: {user['actif']}")
                print(f"   Activation Token: {'✅ Présent' if user.get('activationToken') else '❌ Manquant'}")
                return True
            else:
                print(f"❌ Échec de création: {response.text}")
                return False
        elif response.status_code == 403:
            print(f"⚠️ Admin needs activation - Token: {response.json().get('activationToken')}")
            return False
        else:
            print(f"❌ Échec du login admin: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Erreur: {e}")
        return False

def test_activation_flow():
    """Test 2: Flux d'activation"""
    print("\n" + "="*60)
    print("TEST 2: Flux d'activation du compte")
    print("="*60)
    
    email = "test.encadrant@gmail.com"
    
    # Vérifier le statut
    try:
        response = requests.post(
            f"{BASE_URL}/auth/check-status",
            json={"email": email}
        )
        
        print(f"\nCheck Status - Status: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Statut récupéré:")
            print(f"   Email: {data['email']}")
            print(f"   Needs Activation: {data['needsActivation']}")
            if 'activationToken' in data:
                print(f"   Activation Token: {data['activationToken'][:8]}...")
            
            if data['needsActivation']:
                print(f"\n✅ Le compte nécessite une activation - CORRECT!")
                return True
            else:
                print(f"\n⚠️ Le compte est déjà activé")
                return False
        else:
            print(f"❌ Échec: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Erreur: {e}")
        return False

def test_login_needs_activation():
    """Test 3: Login avec compte non activé"""
    print("\n" + "="*60)
    print("TEST 3: Login avec compte nécessitant activation")
    print("="*60)
    
    login_data = {
        "email": "test.encadrant@gmail.com",
        "password": "nimportequoi"  # N'importe quel password
    }
    
    try:
        response = requests.post(f"{BASE_URL}/auth/login", json=login_data)
        
        print(f"\nLogin - Status: {response.status_code}")
        
        if response.status_code == 403:
            data = response.json()
            if data.get('needsActivation'):
                print(f"✅ Login détecte correctement le besoin d'activation!")
                print(f"   Message: {data['message']}")
                print(f"   Token: {data['activationToken'][:8]}...")
                return True
            else:
                print(f"❌ Response 403 mais needsActivation=False")
                return False
        elif response.status_code == 200:
            print(f"⚠️ Login réussi directement (compte déjà activé?)")
            return False
        else:
            print(f"❌ Erreur inattendue: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Erreur: {e}")
        return False

def test_activate_account():
    """Test 4: Activation du compte"""
    print("\n" + "="*60)
    print("TEST 4: Activation du compte")
    print("="*60)
    
    # D'abord obtenir le token
    try:
        check_response = requests.post(
            f"{BASE_URL}/auth/check-status",
            json={"email": "test.encadrant@gmail.com"}
        )
        
        if check_response.status_code != 200:
            print(f"❌ Impossible de vérifier le statut")
            return False
        
        check_data = check_response.json()
        
        if not check_data.get('needsActivation'):
            print(f"⚠️ Le compte est déjà activé")
            return False
        
        activation_token = check_data['activationToken']
        
        # Activer le compte
        activate_data = {
            "email": "test.encadrant@gmail.com",
            "password": "Test123456!",
            "activationToken": activation_token
        }
        
        response = requests.post(
            f"{BASE_URL}/auth/activate",
            json=activate_data
        )
        
        print(f"\nActivation - Status: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Compte activé avec succès!")
            print(f"   Message: {data['message']}")
            print(f"   Token JWT: {'✅ Présent' if data.get('token') else '❌ Manquant'}")
            print(f"   User: {data['user']['prenom']} {data['user']['nom']}")
            return True
        else:
            print(f"❌ Échec d'activation: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Erreur: {e}")
        return False

def main():
    print("\n" + "🔬 " * 20)
    print("TEST DE LA FONCTIONNALITÉ D'ACTIVATION DES COMPTES")
    print("🔬 " * 20)
    
    results = []
    
    # Test 1: Création d'utilisateur
    results.append(("Création utilisateur", test_create_user()))
    
    # Test 2: Vérification du statut
    results.append(("Vérification statut", test_activation_flow()))
    
    # Test 3: Login avec activation requise
    results.append(("Login needs activation", test_login_needs_activation()))
    
    # Test 4: Activation du compte
    results.append(("Activation compte", test_activate_account()))
    
    # Résumé
    print("\n" + "="*60)
    print("📊 RÉSUMÉ DES TESTS")
    print("="*60)
    
    for test_name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"{status} - {test_name}")
    
    passed = sum(1 for _, r in results if r)
    total = len(results)
    
    print(f"\n{passed}/{total} tests réussis")
    
    if passed == total:
        print("\n🎉 TOUS LES TESTS SONT PASSÉS! La fonctionnalité est PARFAITE!")
    else:
        print(f"\n⚠️ {total - passed} test(s) ont échoué")

if __name__ == "__main__":
    main()
