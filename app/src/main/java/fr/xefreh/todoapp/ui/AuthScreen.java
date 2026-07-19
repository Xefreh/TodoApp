package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fr.xefreh.todoapp.R;

/**
 * Écran d'authentification programmatique (sans XML), gérant à la fois la connexion et
 * l'inscription via un mode {@link Mode}. Cohérent avec les autres écrans (*Screen) du projet.
 *
 * <p>Les champs et boutons sont exposés en champs publics pour que {@code LoginActivity}
 * puisse y câbler les listeners.</p>
 */
public final class AuthScreen {

    /** Mode d'affichage de l'écran. */
    public enum Mode { LOGIN, REGISTER }

    public final LinearLayout root;

    public final TextInputLayout usernameLayout;
    public final TextInputEditText usernameInput;

    public final TextInputLayout passwordLayout;
    public final TextInputEditText passwordInput;

    /** Champ de confirmation de mot de passe — visible uniquement en mode REGISTER. */
    public final TextInputLayout passwordConfirmLayout;
    public final TextInputEditText passwordConfirmInput;

    public final MaterialButton submitButton;
    public final MaterialButton switchModeButton;
    public final TextView heading;

    private Mode mode;

    public AuthScreen(Context context) {
        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = ViewUtils.dp(context, 16);
        root.setPadding(padding, padding, padding, padding);

        // Centre verticalement le contenu dans la fenêtre.
        NestedScrollView scroll = new NestedScrollView(context);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        heading = new TextView(context);
        heading.setTextAppearance(ViewUtils.resolveStyle(
                context, com.google.android.material.R.attr.textAppearanceHeadlineMedium));
        LinearLayout.LayoutParams headingParams = matchWidthWrapHeight();
        headingParams.bottomMargin = padding;
        content.addView(heading, headingParams);

        usernameLayout = new TextInputLayout(
                context, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        usernameLayout.setHint(R.string.hint_username);
        usernameInput = new TextInputEditText(usernameLayout.getContext());
        usernameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        usernameLayout.addView(usernameInput, matchWidthWrapHeight());
        LinearLayout.LayoutParams usernameParams = matchWidthWrapHeight();
        content.addView(usernameLayout, usernameParams);

        passwordLayout = new TextInputLayout(
                context, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        passwordLayout.setHint(R.string.hint_password);
        passwordInput = new TextInputEditText(passwordLayout.getContext());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordLayout.addView(passwordInput, matchWidthWrapHeight());
        LinearLayout.LayoutParams passwordParams = matchWidthWrapHeight();
        passwordParams.topMargin = ViewUtils.dp(context, 12);
        content.addView(passwordLayout, passwordParams);

        passwordConfirmLayout = new TextInputLayout(
                context, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        passwordConfirmLayout.setHint(R.string.hint_password_confirm);
        passwordConfirmInput = new TextInputEditText(passwordConfirmLayout.getContext());
        passwordConfirmInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordConfirmLayout.addView(passwordConfirmInput, matchWidthWrapHeight());
        LinearLayout.LayoutParams confirmParams = matchWidthWrapHeight();
        confirmParams.topMargin = ViewUtils.dp(context, 12);
        content.addView(passwordConfirmLayout, confirmParams);

        submitButton = new MaterialButton(context);
        LinearLayout.LayoutParams submitParams = matchWidthWrapHeight();
        submitParams.topMargin = padding;
        content.addView(submitButton, submitParams);

        switchModeButton = new MaterialButton(
                context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        LinearLayout.LayoutParams switchParams = matchWidthWrapHeight();
        switchParams.topMargin = ViewUtils.dp(context, 4);
        content.addView(switchModeButton, switchParams);

        scroll.addView(content, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        applyMode(Mode.LOGIN);
    }

    /** Bascule entre connexion et inscription et rafraîchit libellés / visibilité des champs. */
    public void applyMode(Mode mode) {
        this.mode = mode;
        boolean register = mode == Mode.REGISTER;
        heading.setText(register ? R.string.title_register : R.string.title_login);
        submitButton.setText(register ? R.string.action_register : R.string.action_login);
        switchModeButton.setText(register
                ? R.string.action_switch_to_login
                : R.string.action_switch_to_register);
        passwordConfirmLayout.setVisibility(register ? View.VISIBLE : View.GONE);
    }

    public Mode getMode() {
        return mode;
    }

    private static LinearLayout.LayoutParams matchWidthWrapHeight() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
