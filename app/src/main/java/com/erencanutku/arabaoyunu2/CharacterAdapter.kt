package com.erencanutku.arabaoyunu2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CharacterAdapter(
    private val context: Context,
    private val characters: List<Character>,
    private val selectedCharacterId: String,
    private val onCharacterSelected: (Character) -> Unit
) : RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    class CharacterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view as CardView
        val characterEmoji: TextView = view.findViewById(R.id.characterEmoji)
        val characterName: TextView = view.findViewById(R.id.characterName)
        val characterAbility: TextView = view.findViewById(R.id.characterAbility)
        val characterStatus: TextView = view.findViewById(R.id.characterStatus)
        val unlockRequirement: TextView = view.findViewById(R.id.unlockRequirement)
        val lockOverlay: View = view.findViewById(R.id.lockOverlay)
        val lockIcon: TextView = view.findViewById(R.id.lockIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_character, parent, false)
        return CharacterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val character = characters[position]

        holder.characterName.text = character.name
        holder.characterAbility.text = character.ability
        holder.characterEmoji.text = character.emoji

        // Handle unlock status and selection
        if (character.isUnlocked) {
            holder.lockOverlay.visibility = View.GONE
            holder.lockIcon.visibility = View.GONE
            holder.unlockRequirement.visibility = View.GONE
            holder.cardView.alpha = 1.0f

            if (character.id == selectedCharacterId) {
                holder.characterStatus.text = "✓ Seçili"
                holder.characterStatus.setBackgroundColor(context.getColor(android.R.color.holo_green_dark))
            } else {
                holder.characterStatus.text = "Seç"
                holder.characterStatus.setBackgroundColor(context.getColor(android.R.color.holo_blue_dark))
            }

            holder.cardView.setOnClickListener {
                onCharacterSelected(character)
            }
        } else {
            holder.lockOverlay.visibility = View.VISIBLE
            holder.lockIcon.visibility = View.VISIBLE
            holder.unlockRequirement.visibility = View.VISIBLE
            holder.unlockRequirement.text = character.unlockRequirement
            holder.characterStatus.text = "🔒 Kilitli"
            holder.characterStatus.setBackgroundColor(context.getColor(android.R.color.darker_gray))
            holder.cardView.alpha = 0.6f

            holder.cardView.setOnClickListener {
                // Show unlock requirement dialog
            }
        }
    }

    override fun getItemCount(): Int = characters.size
}