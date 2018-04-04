package gaydar.struct

class Item
{
  companion object
  {
    private var count = 0
    private fun count() = count--
    val order = mapOf(
          "Item_Weapon_AWM_C" to count(),
          "Item_Weapon_M24_C" to count(),
          "Item_Weapon_Kar98k_C" to count(),
          "Item_Weapon_AUG_C" to count(),
          "Item_Attach_Weapon_Upper_CQBSS_C" to count(),
          "Item_Attach_Weapon_Upper_ACOG_01_C" to count(),
          "Item_Attach_Weapon_Muzzle_Suppressor_SniperRifle_C" to count(),
          "Item_Attach_Weapon_Muzzle_Suppressor_Large_C" to count(),
          "Item_Weapon_M249_C" to count(),
          "Item_Weapon_Mk14_C" to count(),
          "Item_Weapon_Groza_C" to count(),
          "Item_Weapon_HK416_C" to count(),
          "Item_Weapon_SCAR-L_C" to count(),
          "Item_Weapon_Mini14_C" to count(),
          "Item_Weapon_M16A4_C" to count(),
          "Item_Weapon_SKS_C" to count(),
          "Item_Weapon_AK47_C" to count(),
          "Item_Weapon_DP28_C" to count(),
          "Item_Weapon_Saiga12_C" to count(),
          "Item_Weapon_UMP_C" to count(),
          "Item_Weapon_Vector_C" to count(),
          "Item_Weapon_UZI_C" to count(),
          "Item_Weapon_VSS_C" to count(),
          "Item_Weapon_Thompson_C" to count(),
          "Item_Weapon_Berreta686_C" to count(),
          "Item_Weapon_Winchester_C" to count(),
          "Item_Weapon_Win94_C" to count(),
          "Item_Weapon_G18_C" to count(),
          "Item_Weapon_SawenOff_C" to count(),
          "Item_Weapon_Rhino_C" to count(),
          "Item_Weapon_M1911_C" to count(),
          "Item_Weapon_NagantM1895_C" to count(),
          "Item_Weapon_M9_C" to count(),
          "Item_Ghillie_02_C" to count(),
          "Item_Ghillie_01_C" to count(),

          "Item_Ammo_762mm_C" to count(),
          "Item_Ammo_556mm_C" to count(),
          "Item_Ammo_300Magnum_C" to count(),

          "Item_Weapon_Grenade_C" to count(),
          "Item_Heal_MedKit_C" to count(),
          "Item_Heal_FirstAid_C" to count(),
          "Item_Weapon_SmokeBomb_C" to count(),
          "Item_Attach_Weapon_Upper_Holosight_C" to count(),
          "Item_Attach_Weapon_Upper_DotSight_01_C" to count(),
          "Item_Attach_Weapon_Upper_Aimpoint_C" to count(),

          "Item_Attach_Weapon_Muzzle_Compensator_SniperRifle_C" to count(),
          "Item_Attach_Weapon_Muzzle_Compensator_Large_C" to count(),
          "Item_Attach_Weapon_Muzzle_FlashHider_SniperRifle_C" to count(),
          "Item_Boost_PainKiller_C" to count(),
          "Item_Boost_EnergyDrink_C" to count(),
          "Item_Boost_AdrenalineSyringe_C" to count(),
          "Item_Attach_Weapon_Muzzle_FlashHider_Large_C" to count(),
          "Item_Attach_Weapon_Muzzle_Compensator_Medium_C" to count(),

          "Item_Armor_D_01_Lv2_C" to count(),
          "Item_Armor_C_01_Lv3_C" to count(),
          "Item_Head_G_01_Lv3_C" to count(),
          "Item_Head_F_02_Lv2_C" to count(),
          "Item_Head_F_01_Lv2_C" to count(),
          "Item_Back_C_02_Lv3_C" to count(),
          "Item_Back_C_01_Lv3_C" to count(),
          "Item_Back_F_01_Lv2_C" to count(),
          "Item_Back_F_02_Lv2_C" to count(),
          "Item_Back_E_02_Lv1_C" to count(),
          "Item_Back_E_01_Lv1_C" to count(),
          "Item_Armor_E_01_Lv1_C" to count(),
          "Item_Head_E_01_Lv1_C" to count(),
          "Item_Head_E_02_Lv1_C" to count(),

          "Item_Weapon_Molotov_C" to count(),
          "Item_Weapon_FlashBang_C" to count(),
          "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_SniperRifle_C" to count(),
          "Item_Attach_Weapon_Magazine_Extended_SniperRifle_C" to count(),
          "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Large_C" to count(),
          "Item_Attach_Weapon_Magazine_Extended_Large_C" to count(),

          "Item_Attach_Weapon_Stock_SniperRifle_CheekPad_C" to count(),
          "Item_Attach_Weapon_Stock_SniperRifle_BulletLoops_C" to count(),
          "Item_Attach_Weapon_Stock_AR_Composite_C" to count(),
          "Item_Attach_Weapon_Muzzle_Suppressor_Medium_C" to count(),
          "Item_Attach_Weapon_Muzzle_FlashHider_Medium_C" to count(),
          "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Medium_C" to count(),
          "Item_Attach_Weapon_Magazine_Extended_Medium_C" to count(),

          "Item_Attach_Weapon_Lower_Foregrip_C" to count(),
          "Item_Attach_Weapon_Lower_AngledForeGrip_C" to count(),

          "Item_Weapon_Pan_C" to count(),
          "Item_Ammo_9mm_C" to count(),
          "Item_Heal_Bandage_C" to count(),
          "Item_Ammo_45ACP_C" to count(),
          "Item_Ammo_12Guage_C" to count(),

          "Item_Weapon_FlareGun_C" to count(),
          "Item_Ammo_Flare_C" to count()


                     )

    val category = mapOf(
          "Item" to mapOf(
                "Boost" to mapOf(
                      "PainKiller" to "(PainKiller)",
                      "EnergyDrink" to "(EnergyDrink)",
                      "Adrenaline" to "(Adrenaline)"
                                ),
                "Heal" to mapOf(
                      "FirstAid" to "(FirstAid)",
                      "MedKit" to "(MedKit)",
                      "Bandage" to "(Bandage)"
                               ),
                "Armor" to mapOf(
                      "C" to "LEVEL 3 ARMOR",
                      "D" to "Level 2 Armor",
                      "E" to "Level 1 Armor"
                                ),
                "Back" to mapOf(
                      "B" to "Parachute",
                      "C" to mapOf(
                            "01" to "LEVEL 3 BAG",
                            "02" to "LEVEL 3 BAG"
                                  ),
                      "F" to mapOf(
                            "01" to "Level 2 Bag",
                            "02" to "Level 1 Bag"
                                  ),
                      "E" to mapOf(
                            "01" to "Level 1 Bag",
                            "02" to "Level 1 Bag"
                                  )
                               ),
                "Head" to mapOf(
                      "G" to "LEVEL 3 HELMET",
                      "F" to mapOf(
                            "01" to "Level 2 Helmet",
                            "02" to "Level 2 Helmet"
                                  ),
                      "E" to mapOf(
                            "01" to "Level 1 Helmet",
                            "02" to "Level 1 Helmet"
                                  )
                               )
                         )
                        )

    fun simplify(description : String) : String
    {
      try
      {
        val words = description.split("_")
        var c : Map<*, *> = category
        for (word in words)
        {
          if (word !in c)
            return description
          val sub : Any? = c[word]
          if (sub is String)
            return sub
          c = sub as Map<*, *>
        }
      }
      catch (e : Exception)
      {
      }
      return description
    }

  }
}