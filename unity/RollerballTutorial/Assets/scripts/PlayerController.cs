using UnityEngine;
using System.Collections;

public class PlayerController : MonoBehaviour {

	public float speed;
	public GUIText countText;
	public GUIText winText;
	private int count;

	// Use this for initialization
	void Start () {
		count = 0;
		UpdateCount ();
		winText.text = "";
	}

	void OnTriggerEnter(Collider other) {
		if (other.gameObject.tag == "Pickup") {
			other.gameObject.SetActive (false);
			count = count + 1;
			UpdateCount ();
		}
	}

	void UpdateCount() {
		countText.text = "Count: " + count.ToString ();
		if (count >= 13) {
			winText.text = "You Win!";
		}
	}

	void FixedUpdate() {
		float moveHorizontal = Input.GetAxis ("Horizontal");
		float moveVertical = Input.GetAxis ("Vertical");

		Vector3 movement = new Vector3 (moveHorizontal, 0f, moveVertical);

		rigidbody.AddForce (movement * speed * Time.deltaTime);
	}
	
	// Update is called once per frame
	void Update () {
	
	}
}
