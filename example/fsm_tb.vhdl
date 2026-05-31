library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity fsm_new_tb is
end entity;

architecture sim of fsm_new_tb is
    constant c_clk_period : time := 10 ns;
    signal clk : std_logic := '0';
    signal rst : std_logic := '0';
    signal inp1 : std_logic := '0';
    signal out2 : std_logic_vector(1 downto 0);
begin

    clk <= not clk after c_clk_period/2;

    uut: entity work.fsm_new
    port map(
        clk => clk,
        rst => rst,
        inp1 => inp1,
        out2 => out2
    );

    stim: process is
        variable v : integer := 0;
    begin
        rst <= '0';
        wait for 3*c_clk_period;
        rst <= '1';
        wait for c_clk_period;

        -- drives up to 2 input vectors
        for v in 0 to 1 loop
            if ((v / 1) mod 2) = 1 then
                inp1 <= '1';
            else
                inp1 <= '0';
            end if;
            wait for 2*c_clk_period;
        end loop;

        wait;
    end process;
end architecture;
