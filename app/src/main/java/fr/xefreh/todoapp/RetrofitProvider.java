package fr.xefreh.todoapp;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitProvider {
	private static Retrofit retrofit;

	public static JsonPlaceholderApi getApi() {
		if (retrofit == null) {
			retrofit = new Retrofit.Builder()
					.baseUrl("https://jsonplaceholder.typicode.com/")
					.addConverterFactory(GsonConverterFactory.create())
					.build();
		}
		return retrofit.create(JsonPlaceholderApi.class);
	}
}